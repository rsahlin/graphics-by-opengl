package com.nucleus.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.bounds.Bounds;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.common.TypeResolver;
import com.nucleus.exporter.NodeExporter;
import com.nucleus.exporter.NucleusNodeExporter;
import com.nucleus.geometry.MeshFactory;
import com.nucleus.io.gson.BoundsDeserializer;
import com.nucleus.io.gson.NucleusNodeDeserializer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.BaseRootNode;
import com.nucleus.scene.DefaultNodeFactory;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.NodeFactory;
import com.nucleus.scene.NodeType;
import com.nucleus.scene.RootNode;

/**
 * GSON Serializer for nucleus scenegraph.
 * Do not create this class directly use {@linkplain SceneSerializerFactory}
 * 
 * @author Richard Sahlin
 *
 */
public class GSONSceneFactory implements SceneSerializer {

    protected ArrayDeque<LayerNode> viewStack = new ArrayDeque<LayerNode>(NucleusRenderer.MIN_STACKELEMENTS);
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
     * This constructor will call {@link #init(NucleusRenderer, NodeFactory, MeshFactory)}
     * 
     * @param renderer
     * @param nodeFactory
     * @param meshFactory
     * @param types The types to be registered {@linkplain TypeResolver}
     */
    public GSONSceneFactory(NucleusRenderer renderer, NodeFactory nodeFactory, MeshFactory meshFactory,
            List<Type<?>> types) {
        createNodeExporter();
        registerNodeExporters();
        init(renderer, nodeFactory, meshFactory);
        TypeResolver.getInstance().registerTypes(types);
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
    public RootNode importScene(String filename) throws NodeException {
        ClassLoader loader = getClass().getClassLoader();
        InputStream is = loader.getResourceAsStream(filename);
        try {
            return importScene(is);
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
    public RootNode importScene(InputStream is) throws NodeException {
        if (renderer == null || nodeFactory == null) {
            throw new IllegalStateException(INIT_NOT_CALLED_ERROR);
        }
        if (is == null) {
            throw new IllegalArgumentException(NULL_PARAMETER_ERROR + "inputstream");
        }
        try {
            Reader reader = new InputStreamReader(is, "UTF-8");
            GsonBuilder builder = new GsonBuilder();
            // First register type adapters - then call GsonBuilder.create() to build a Gson instance
            // using the specified adapters
            registerTypeAdapter(builder);
            setGson(builder.create());
            RootNode scene = getSceneFromJson(gson, reader);
            RootNode createdRoot = createScene(scene.getResources(), scene.getScene());
            return createdRoot;
        } catch (IOException e) {
            throw new NodeException(e);
        }
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
     * @throws NodeException
     */
    private RootNode createScene(ResourcesData resources, Node scene) throws NodeException {
        RootNode root = createInstance(resources);
        addNodes(resources, root, scene);
        return root;
    }

    /**
     * Creates a new {@linkplain RootNode} for the specified scene, containing the layer nodes.
     * 
     * @return The root node implementation to use
     */
    protected RootNode createInstance() {
        return new BaseRootNode();
    }

    /**
     * Creates a new root node and copying the resources into the new root.
     * 
     * @param resource
     * @return
     */
    protected RootNode createInstance(ResourcesData resource) {
        RootNode root = createInstance();
        root.getResources().copy(resource);
        return root;
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
    private Node addNodes(ResourcesData resources, RootNode root, Node node) throws NodeException {
        Node created = nodeFactory.create(renderer, meshFactory, resources, node, root);
        root.setScene(created);
        setViewFrustum(node, created);
        nodeFactory.createChildNodes(renderer, meshFactory, resources, node, created);
        created.onCreated();
        return created;
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
