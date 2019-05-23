package com.nucleus.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.SimpleLogger;
import com.nucleus.common.Type;
import com.nucleus.common.TypeResolver;
import com.nucleus.exporter.NodeExporter;
import com.nucleus.exporter.NucleusNodeExporter;
import com.nucleus.io.gson.GLTFDeserializerImpl;
import com.nucleus.io.gson.NucleusDeserializer;
import com.nucleus.io.gson.NucleusDeserializerImpl;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.AbstractNode;
import com.nucleus.scene.AbstractNode.NodeTypes;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeBuilder;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.RootNodeBuilder;
import com.nucleus.scene.RootNodeImpl;

/**
 * GSON Serializer for scenes based on JSON.
 * 
 * 
 * @author Richard Sahlin
 *
 */
public class GSONSceneFactory implements SceneSerializer<RootNode> {

    protected ArrayDeque<LayerNode> viewStack = new ArrayDeque<LayerNode>(NucleusRenderer.MIN_STACKELEMENTS);
    protected HashMap<String, NucleusDeserializer<?>> deserializers = new HashMap<>();

    private final static String ERROR_CLOSING_STREAM = "Error closing stream:";
    private final static String NULL_PARAMETER_ERROR = "Parameter is null: ";
    public final static String NOT_IMPLEMENTED = "Not implemented: ";
    private final static String WRONG_CLASS_ERROR = "Wrong class: ";

    protected NucleusRenderer renderer;
    protected NodeExporter nodeExporter;

    /**
     * Use as singleton since serializers depend on {@link TypeResolver} (which is also singleton)
     */
    protected static SceneSerializer<RootNode> sceneFactory;

    /**
     * Returns the singleton instance of sceneserializer
     * 
     * @param renderer
     * @return
     */
    public static SceneSerializer<RootNode> getInstance() {
        if (sceneFactory == null) {
            sceneFactory = new GSONSceneFactory();
        }
        return sceneFactory;
    }

    protected GSONSceneFactory() {
    }

    /**
     * Returns the {@link NucleusDeserializer} to use with the specified type.
     * Use this to know what deserializer to use for different scene/file types
     * 
     * @param type The file/scene type
     */
    protected NucleusDeserializer<?> getDeserializer(String type) {
        NucleusDeserializer<?> deserializer = deserializers.get(type);
        if (deserializer == null) {
            deserializer = createNodeDeserializer(type);
            createGSON(deserializer);
            deserializers.put(type, deserializer);
        }
        return deserializer;
    }

    /**
     * Creates a new instance of the deserializer for the scene/file type
     * 
     * @param type
     * @return
     */
    protected NucleusDeserializer<?> createNodeDeserializer(String type) {
        if (type.contentEquals(RootNodeBuilder.NUCLEUS_SCENE)) {
            return createNucleusNodeDeserializer();
        }
        if (type.contentEquals(RootNodeBuilder.GLTF_SCENE)) {
            return new GLTFDeserializerImpl();
        }
        return null;
    }

    protected NucleusDeserializer<Node> createNucleusNodeDeserializer() {
        return new NucleusDeserializerImpl();
    }

    @Override
    public void init(NucleusRenderer renderer, Type<?>[] types) {
        if (renderer == null) {
            throw new IllegalArgumentException(NULL_RENDERER_ERROR);
        }
        this.renderer = renderer;
        if (types != null) {
            registerTypes(types);
        }
    }

    @Override
    public RootNode importScene(String path, String filename, String type, NodeInflaterListener inflaterLister)
            throws NodeException {
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path = path + File.pathSeparator;
        }
        SimpleLogger.d(getClass(), "Importing scene:" + path + filename);
        ClassLoader loader = getClass().getClassLoader();
        InputStream is = loader.getResourceAsStream(path + filename);
        try {
            RootNode root = importScene(path, is, type, inflaterLister);
            return root;
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

    private RootNode importScene(String path, InputStream is, String type, NodeInflaterListener inflaterListener)
            throws NodeException {
        if (!isInitialized()) {
            throw new IllegalStateException(INIT_NOT_CALLED_ERROR);
        }
        if (is == null) {
            throw new IllegalArgumentException(NULL_PARAMETER_ERROR + "inputstream");
        }
        try {
            NucleusDeserializer<?> deserializer = getDeserializer(type);
            long start = System.currentTimeMillis();
            Reader reader = new InputStreamReader(is, "UTF-8");
            RootNode root = importFromGSON(reader, deserializer, RootNodeBuilder.getTypeClass(type));
            root.setNodeInflaterListener(inflaterListener);
            long loaded = System.currentTimeMillis();
            FrameSampler.getInstance().logTag(FrameSampler.Samples.LOAD_SCENE, start, loaded);
            RootNode createdRoot = root.createInstance();
            createScene(createdRoot, root.getChildren());
            FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_SCENE, loaded, System.currentTimeMillis());
            return createdRoot;
        } catch (IOException e) {
            throw new NodeException(e);
        }
    }

    protected void createGSON(NucleusDeserializer<?> deserializer) {
        GsonBuilder builder = new GsonBuilder();
        // First register type adapters - then call GsonBuilder.create() to build a Gson instance
        // using the specified adapters
        deserializer.registerTypeAdapter(builder);
        Gson gson = builder.create();
        deserializer.setGson(gson);
    }

    /**
     * Imports gson into BaseRootNode
     * 
     * @param reader
     * @param deserializer
     * @param classOffT
     * @return Scene root with data loaded.
     * @throws UnsupportedEncodingException
     */
    protected RootNode importFromGSON(Reader reader, NucleusDeserializer<?> deserializer,
            java.lang.reflect.Type classOffT)
            throws IOException {
        Gson gson = deserializer.getGson();
        RootNodeImpl root = gson.fromJson(reader, classOffT);
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
     * Internal method to create a layer node and add it to the rootnode.
     * 
     * @param root The root node that the created node will be added to.
     * @param children The root childnodes to create
     * @return
     * @throws IOException
     */
    private void createNodes(RootNode root, List<Node> children) throws NodeException {
        NodeBuilder<Node> builder = new NodeBuilder<Node>();
        builder.setRoot(root);
        for (Node node : children) {
            builder.createRoot(renderer, node);
        }
    }

    @Override
    public void exportScene(OutputStream out, Object obj) throws IOException {
        if (!(obj instanceof RootNodeImpl)) {
            throw new IllegalArgumentException(WRONG_CLASS_ERROR + obj.getClass().getName());
        }
        RootNodeImpl root = (RootNodeImpl) obj;
        // First create the rootnode to hold resources and instances.
        // Subclasses may need to override to return correct instance of SceneData
        // RootNodeImpl rootNode = createSceneData();

        // nodeExporter.exportNodes(root, rootNode);

        // Gson gson = new GsonBuilder().create();
        // out.write(gson.toJson(rootNode).getBytes());

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

    @Override
    public boolean isInitialized() {
        return (renderer != null);
    }

    @Override
    public void registerTypes(Type<?>[] types) {
        TypeResolver.getInstance().registerTypes(types);
    }

}
