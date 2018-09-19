package com.nucleus.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.SimpleLogger;
import com.nucleus.bounds.Bounds;
import com.nucleus.common.Type;
import com.nucleus.common.TypeResolver;
import com.nucleus.exporter.NodeExporter;
import com.nucleus.exporter.NucleusNodeExporter;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.io.gson.BoundsDeserializer;
import com.nucleus.io.gson.NucleusNodeDeserializer;
import com.nucleus.io.gson.ShapeDeserializer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.AbstractNode;
import com.nucleus.scene.AbstractNode.NodeTypes;
import com.nucleus.scene.BaseRootNode;
import com.nucleus.scene.DefaultNodeFactory;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeBuilder;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.NodeFactory;
import com.nucleus.scene.RenderableNode;
import com.nucleus.scene.RootNode;
import com.nucleus.vecmath.Shape;

/**
 * GSON Serializer for nucleus scenegraph.
 * 
 * @author Richard Sahlin
 *
 */
public class GSONSceneFactory implements SceneSerializer {

    protected ArrayDeque<LayerNode> viewStack = new ArrayDeque<LayerNode>(NucleusRenderer.MIN_STACKELEMENTS);
    protected NucleusNodeDeserializer nodeDeserializer;
    protected BoundsDeserializer boundsDeserializer = new BoundsDeserializer();
    protected ShapeDeserializer shapeDeserializer = new ShapeDeserializer();

    private final static String ERROR_CLOSING_STREAM = "Error closing stream:";
    private final static String NULL_PARAMETER_ERROR = "Parameter is null: ";
    public final static String NOT_IMPLEMENTED = "Not implemented: ";
    private final static String WRONG_CLASS_ERROR = "Wrong class: ";

    protected GLES20Wrapper gles;
    protected NodeExporter nodeExporter;
    protected NodeFactory nodeFactory;

    protected Gson gson;

    /**
     * Use as singleton since serializers depend on {@link TypeResolver} (which is also singleton)
     */
    protected static SceneSerializer sceneFactory;

    /**
     * Returns the singleton instance of sceneserializer
     * 
     * @param renderer
     * @return
     */
    public static SceneSerializer getInstance() {
        if (sceneFactory == null) {
            sceneFactory = new GSONSceneFactory();
        }
        return sceneFactory;
    }

    protected GSONSceneFactory() {
    }

    /**
     * Creates the instance of the {@link NucleusNodeDeserializer} to be used, called from constructor
     * Override in subclasses to change
     */
    protected void createNodeDeserializer() {
        nodeDeserializer = new NucleusNodeDeserializer();
    }

    @Override
    public void init(GLES20Wrapper gles, NodeFactory nodeFactory, Type<?>[] types) {
        if (gles == null) {
            throw new IllegalArgumentException(NULL_GLES_ERROR);
        }
        if (nodeFactory == null) {
            throw new IllegalArgumentException(NULL_NODEFACTORY_ERROR);
        }
        this.gles = gles;
        this.nodeFactory = nodeFactory;
        createNodeDeserializer();
        if (types != null) {
            registerTypes(types);
        }
    }

    @Override
    public RootNode importScene(String path, String filename) throws NodeException {
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path = path + File.pathSeparator;
        }
        SimpleLogger.d(getClass(), "Importing scene:" + path + filename);
        ClassLoader loader = getClass().getClassLoader();
        InputStream is = loader.getResourceAsStream(path + filename);
        try {
            RootNode scene = importScene(path, is);
            return scene;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Cannot do anything.
                    SimpleLogger.d(getClass(), ERROR_CLOSING_STREAM + e.getMessage());
                }
            }
        }
    }

    private RootNode importScene(String path, InputStream is) throws NodeException {
        if (!isInitialized()) {
            throw new IllegalStateException(INIT_NOT_CALLED_ERROR);
        }
        if (is == null) {
            throw new IllegalArgumentException(NULL_PARAMETER_ERROR + "inputstream");
        }
        try {
            long start = System.currentTimeMillis();
            Reader reader = new InputStreamReader(is, "UTF-8");
            GsonBuilder builder = new GsonBuilder();
            // First register type adapters - then call GsonBuilder.create() to build a Gson instance
            // using the specified adapters
            registerTypeAdapter(builder);
            setGson(builder.create());
            RootNode scene = importFromGSON(path, gson, reader);
            long loaded = System.currentTimeMillis();
            FrameSampler.getInstance().logTag(FrameSampler.Samples.LOAD_SCENE, start, loaded);
            RootNode root = createRoot();
            scene.copyTo(root);
            createScene(root, scene.getChildren());
            FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_SCENE, loaded, System.currentTimeMillis());
            return root;
        } catch (IOException e) {
            throw new NodeException(e);
        }
    }

    /**
     * Register type adapter(s), implement in subclasses as needed and call super
     * 
     * @param builder
     */
    protected void registerTypeAdapter(GsonBuilder builder) {
        builder.registerTypeAdapter(Bounds.class, boundsDeserializer);
        builder.registerTypeAdapter(Node.class, nodeDeserializer);
        builder.registerTypeAdapter(Shape.class, shapeDeserializer);
    }

    /**
     * Utility method to get the default nodefactory
     * 
     * @return
     */
    public static NodeFactory getNodeFactory() {
        return new DefaultNodeFactory();
    }

    /**
     * Imports gson into BaseRootNode
     * TODO: Add type selector so that rootnode impl can be specified in scene
     * 
     * @param gson
     * @param reader
     * @return Scene root with data loaded.
     * @throws UnsupportedEncodingException
     */
    protected RootNode importFromGSON(String path, Gson gson, Reader reader) throws IOException {
        RootNode root = gson.fromJson(reader, BaseRootNode.class);
        return root;
    }

    /**
     * Creates instances of the nodes in the scene and adds to the root, use this method when importing to create
     * a new instance of the loaded scene.
     * 
     * @param root The root node, created child nodes will be added to this
     * @param scene The root nodes to create instances of
     * @return The created scenes or null if there is an error.
     * @throws NodeException
     */
    private RootNode createScene(RootNode root, List<Node> scene) throws NodeException {
        createNodes(root, scene);
        return root;
    }

    /**
     * Creates a new {@linkplain RootNode} for the specified scene, containing the layer nodes.
     * 
     * @return The root node implementation to use
     */
    protected RootNode createRoot() {
        return new BaseRootNode();
    }

    /**
     * Internal method to create a layer node and add it to the rootnode.
     * 
     * @param root The root node that the created node will be added to.
     * @param children The root childnodes to create
     * @return
     * @throws IOException
     */
    private void createNodes(RootNode root, List<Node> children) throws NodeException {
       NodeBuilder<Node> builder = new NodeBuilder<>();
        builder.setRoot(root);
        for (Node node : children) {
            createNode(gles, node, root, builder, root);
/*
            Node created = nodeFactory.create(renderer, node, root);
            root.addChild(created);
            created.onCreated();
            nodeFactory.createChildNodes(renderer, node, created);
*/            
        }
    }

    private Node createNode(GLES20Wrapper gles, Node source, Node parent, NodeBuilder<Node> builder, RootNode root) throws NodeException {
        Node created = source.createInstance(root);
        created.create();
        try {
            if (source instanceof RenderableNode<?>) {
                RenderableNode rNode = (RenderableNode<?>) created;
                MeshBuilder<?> meshBuilder = rNode.createMeshBuilder(gles, null);
                NodeBuilder.createMesh(meshBuilder, created, 1);
            }
            parent.addChild(created);
            created.onCreated();
            // Recursively create children if there are any
            if (source.getChildren() != null) {
                for (Node nd : source.getChildren()) {
                    createNode(gles, nd, created, builder, root);
                }
            }
            return created;
        } catch (IOException | GLException e) {
            throw new NodeException(e);
        }
        
    }
    
    @Override
    public void exportScene(OutputStream out, Object obj) throws IOException {
        if (!(obj instanceof RootNode)) {
            throw new IllegalArgumentException(WRONG_CLASS_ERROR + obj.getClass().getName());
        }
        RootNode root = (RootNode) obj;
        // First create the rootnode to hold resources and instances.
        // Subclasses may need to override to return correct instance of SceneData
        RootNode rootNode = createSceneData();

        nodeExporter.exportNodes(root, rootNode);

        Gson gson = new GsonBuilder().create();
        out.write(gson.toJson(rootNode).getBytes());

    }

    /**
     * Creates the correct scenedata implementation, subclasses must implement this method as needed
     * 
     * @return The SceneData implementation to use for the Serializer
     */
    protected RootNode createSceneData() {
        return new BaseRootNode();
    }

    /**
     * Creates the correct instance of the node exporter
     * Subclasses must implement this to create proper exporter instance, do not call super in subclasses.
     */
    protected void createNodeExporter() {
        nodeExporter = new NucleusNodeExporter();
    }

    /**
     * Registers the nodetypes and nodeexporter for the scenfactory using the {@link AbstractNode} implementation,
     * implement in subclasses and call super.
     */
    protected void registerNodeExporters() {
        nodeExporter.registerNodeExporter(NodeTypes.values(), new NucleusNodeExporter());
    }

    /**
     * Set the gson instance to be used, this is called after {@link #registerTypeAdapter(GsonBuilder)}
     * 
     * @param gson
     */
    protected void setGson(Gson gson) {
        this.gson = gson;
        nodeDeserializer.setGson(gson);
        boundsDeserializer.setGson(gson);
        shapeDeserializer.setGson(gson);
    }

    @Override
    public void addNodeType(Type<Node> type) {
        nodeDeserializer.addNodeType(type);
    }

    @Override
    public void addNodeTypes(Type<Node>[] types) {
        nodeDeserializer.addNodeTypes(types);
    }

    @Override
    public boolean isInitialized() {
        return (gles != null && nodeFactory != null);
    }

    @Override
    public void registerTypes(Type<?>[] types) {
        TypeResolver.getInstance().registerTypes(types);
    }

}
