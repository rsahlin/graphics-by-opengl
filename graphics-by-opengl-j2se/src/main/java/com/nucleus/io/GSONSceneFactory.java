package com.nucleus.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayDeque;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.bounds.Bounds;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.exporter.NodeExporter;
import com.nucleus.exporter.NucleusNodeExporter;
import com.nucleus.geometry.MeshFactory;
import com.nucleus.io.gson.BoundsDeserializer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.BaseRootNode;
import com.nucleus.scene.DefaultNodeFactory;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeFactory;
import com.nucleus.scene.NodeType;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.ViewNode;

/**
 * GSON Serializer for nucleus scenegraph.
 * 
 * @author Richard Sahlin
 *
 */
public class GSONSceneFactory implements SceneSerializer {

    protected ArrayDeque<ViewNode> viewStack = new ArrayDeque<ViewNode>(NucleusRenderer.MIN_STACKELEMENTS);
    private NucleusNodeDeserializer nodeDeserializer = new NucleusNodeDeserializer();

    private final static String ERROR_CLOSING_STREAM = "Error closing stream:";
    private final static String NULL_PARAMETER_ERROR = "Parameter is null: ";
    public final static String NOT_IMPLEMENTED = "Not implemented: ";
    private final static String WRONG_CLASS_ERROR = "Wrong class: ";

    protected NucleusRenderer renderer;
    protected NodeExporter nodeExporter;
    protected NodeFactory nodeFactory;
    protected MeshFactory meshFactory;

    protected Gson gson;

    /**
     * Creates a default scenefactory with {@link NucleusNodeExporter}.
     * Calls {@link #createNodeExporter()} and {@link #registerNodeExporters()}
     */
    public GSONSceneFactory() {
        createNodeExporter();
        registerNodeExporters();
    }

    @Override
    public void init(NucleusRenderer renderer, NodeFactory nodeFactory, MeshFactory meshFactory) {
        if (renderer == null) {
            throw new IllegalArgumentException(NULL_RENDERER_ERROR);
        }
        if (nodeFactory == null) {
            throw new IllegalArgumentException(NULL_NODEFACTORY_ERROR);
        }
        if (meshFactory == null) {
            throw new IllegalArgumentException(NULL_MESHFACTORY_ERROR);
        }
        this.renderer = renderer;
        this.nodeFactory = nodeFactory;
        this.meshFactory = meshFactory;
    }

    @Override
    public RootNode importScene(String filename) throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        InputStream is = loader.getResourceAsStream(filename);
        try {
            return importScene(loader.getResourceAsStream(filename));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Cannot do anything.
                    System.out.println(ERROR_CLOSING_STREAM + e.getMessage());
                }
            }
        }
    }

    @Override
    public RootNode importScene(InputStream is) throws IOException {
        if (renderer == null || nodeFactory == null) {
            throw new IllegalStateException(INIT_NOT_CALLED_ERROR);
        }
        if (is == null) {
            throw new IllegalArgumentException(NULL_PARAMETER_ERROR + "inputstream");
        }
        Reader reader = new InputStreamReader(is, "UTF-8");
        GsonBuilder builder = new GsonBuilder();
        registerTypeAdapter(builder);
        setGson(builder.create());
        RootNode scene = getSceneFromJson(gson, reader);
        RootNode createdRoot = createScene(scene.getResources(), scene.getScene());
        return createdRoot;
    }

    /**
     * Retister type adapter(s), implement in subclasses as needed and call super
     * 
     * @param builder
     */
    protected void registerTypeAdapter(GsonBuilder builder) {
        builder.registerTypeAdapter(Bounds.class, new BoundsDeserializer());
        builder.registerTypeAdapter(Node.class, nodeDeserializer);
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
     * Returns the correct implementation of SceneData class, subclasses will override
     * 
     * @param gson
     * @param reader
     * @param classT
     * @return
     */
    protected RootNode getSceneFromJson(Gson gson, Reader reader) {
        return gson.fromJson(reader, BaseRootNode.class);
    }

    /**
     * Creates {@linkplain RootNode} from the scene node and returns, use this method when importing to create
     * a new instance of the loaded scene.
     * 
     * @param resource The resources for the scene
     * @param scene The root scene node
     * @return The created scene or null if there is an error.
     * @throws IOException
     */
    private RootNode createScene(ResourcesData resources, Node scene) throws IOException {
        RootNode root = createRoot();
        addNodes(resources, root, scene);
        return root;
    }

    /**
     * Creates a new {@linkplain RootNode} for the specified scene, containing the layer nodes.
     * The layer nodes will have the new root as its rootnode.
     * 
     * @return The root node implementation to use
     */
    protected RootNode createRoot() {
        return new BaseRootNode();
    }

    /**
     * Internal method to create a layer node and add it to the rootnode.
     * 
     * @param resources The resources in the scene
     * @param root The root node that the created node will be added to.
     * @param node The node to create
     * @return
     * @throws IOException
     */
    private Node addNodes(ResourcesData resources, RootNode root, Node node) throws IOException {
        Node created = nodeFactory.create(renderer, meshFactory, resources, node);
        root.setScene(created);
        setViewFrustum(node, created);
        createChildNodes(resources, node, created);
        created.onCreated();
        return created;
    }

    /**
     * Creates a new node from the source node, creating child nodes as well, looking up resources as needed.
     * The new node will be returned, it is not added to the parent node - this shall be done by the caller.
     * The new node will have parent as its parent node
     * 
     * @param resources The scene resources
     * @param source The node source,
     * @param parent The parent node
     * @return The created node, this will be a new instance of the source node ready to be rendered/processed
     */
    protected Node createNode(ResourcesData resources, Node source, Node parent) throws IOException {
        Node created = nodeFactory.create(renderer, meshFactory, resources, source);
        boolean isViewNode = false;
        if (NodeType.viewnode.name().equals(created.getType())) {
            viewStack.push((ViewNode) created);
            isViewNode = true;
        }
        created.setRootNode(parent.getRootNode());
        setViewFrustum(source, created);
        createChildNodes(resources, source, created);
        if (isViewNode) {
            viewStack.pop();
        }
        return created;

    }

    protected void createChildNodes(ResourcesData resources, Node source, Node parent) throws IOException {
        // Recursively create children
        for (Node nd : source.getChildren()) {
            Node child = createNode(resources, nd, parent);
            if (child != null) {
                parent.addChild(child);
            }
        }

    }

    /**
     * Checks if the node data has viewfrustum data, if it has it is set in the node.
     * 
     * @param source The source node containing the viewfrustum
     * @param node Node to check, or null
     */
    protected void setViewFrustum(Node source, Node node) {
        if (node == null) {
            return;
        }
        ViewFrustum projection = source.getViewFrustum();
        if (projection == null) {
            return;
        }
        node.setViewFrustum(new ViewFrustum(projection));
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
     * Registers the nodetypes and nodeexporter for the scenfactory, implement in subclasses and call super.
     */
    protected void registerNodeExporters() {
        nodeExporter.registerNodeExporter(NodeType.values(), new NucleusNodeExporter());
    }

    /**
     * Set the gson instance to be used, this is called after {@link #registerTypeAdapter(GsonBuilder)}
     * 
     * @param gson
     */
    protected void setGson(Gson gson) {
        this.gson = gson;
        nodeDeserializer.setGson(gson);
    }

}
