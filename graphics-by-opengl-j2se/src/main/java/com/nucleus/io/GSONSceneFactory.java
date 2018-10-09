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
import com.nucleus.io.gson.NucleusDeserializer;
import com.nucleus.io.gson.NucleusNodeDeserializer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.AbstractNode;
import com.nucleus.scene.AbstractNode.NodeTypes;
import com.nucleus.scene.BaseRootNode;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeBuilder;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.RootNode;

/**
 * GSON Serializer for scenes based on JSON.
 * 
 * 
 * @author Richard Sahlin
 *
 */
public class GSONSceneFactory implements SceneSerializer<RootNode> {

    public static final String NUCLEUS_SCENE = "nucleusScene";
    public static final String GLTF_SCENE = "gltfScene";

    protected ArrayDeque<LayerNode> viewStack = new ArrayDeque<LayerNode>(NucleusRenderer.MIN_STACKELEMENTS);
    protected HashMap<String, NucleusDeserializer<?>> deserializers = new HashMap<>();

    private final static String ERROR_CLOSING_STREAM = "Error closing stream:";
    private final static String NULL_PARAMETER_ERROR = "Parameter is null: ";
    public final static String NOT_IMPLEMENTED = "Not implemented: ";
    private final static String WRONG_CLASS_ERROR = "Wrong class: ";

    protected GLES20Wrapper gles;
    protected NodeExporter nodeExporter;

    protected Gson gson;

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
     */
    protected NucleusDeserializer<?> getNodeDeserializer(String type) {
        NucleusDeserializer<?> deserializer = deserializers.get(type);
        if (deserializer == null) {
            deserializer = createNodeDeserializer(type);
            deserializers.put(type, deserializer);
        }
        return deserializer;
    }

    protected NucleusDeserializer<?> createNodeDeserializer(String type) {
        if (type.contentEquals(NUCLEUS_SCENE)) {
            return createNucleusNodeDeserializer();
        }
        if (type.contentEquals(GLTF_SCENE)) {

        }
        return null;
    }

    protected NucleusDeserializer<Node> createNucleusNodeDeserializer() {
        return new NucleusNodeDeserializer();
    }

    @Override
    public void init(GLES20Wrapper gles, Type<?>[] types) {
        if (gles == null) {
            throw new IllegalArgumentException(NULL_GLES_ERROR);
        }
        this.gles = gles;
        if (types != null) {
            registerTypes(types);
        }
    }

    @Override
    public RootNode importScene(String path, String filename, String type) throws NodeException {
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path = path + File.pathSeparator;
        }
        SimpleLogger.d(getClass(), "Importing scene:" + path + filename);
        ClassLoader loader = getClass().getClassLoader();
        InputStream is = loader.getResourceAsStream(path + filename);
        try {
            RootNode scene = importScene(path, is, type);
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

    private RootNode importScene(String path, InputStream is, String type) throws NodeException {
        if (!isInitialized()) {
            throw new IllegalStateException(INIT_NOT_CALLED_ERROR);
        }
        if (is == null) {
            throw new IllegalArgumentException(NULL_PARAMETER_ERROR + "inputstream");
        }
        try {
            NucleusDeserializer<?> deserializer = getNodeDeserializer(type);
            gson = createGSON(deserializer);
            long start = System.currentTimeMillis();
            Reader reader = new InputStreamReader(is, "UTF-8");
            RootNode scene = importFromGSON(path, gson, reader, type);
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

    protected Gson createGSON(NucleusDeserializer<?> deserializer) {
        GsonBuilder builder = new GsonBuilder();
        // First register type adapters - then call GsonBuilder.create() to build a Gson instance
        // using the specified adapters
        deserializer.registerTypeAdapter(builder);
        Gson gson = builder.create();
        deserializer.setGson(gson);
        return gson;
    }

    /**
     * Imports gson into BaseRootNode
     * 
     * @param path
     * @param gson
     * @param reader
     * @param type The type of file - use this to find encapsulating class.
     * @return Scene root with data loaded.
     * @throws UnsupportedEncodingException
     */
    protected RootNode importFromGSON(String path, Gson gson, Reader reader, String type) throws IOException {
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
     * Creates a new {@linkplain RootNode} for the specified scene
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
            builder.create(gles, node, root);
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

    @Override
    public boolean isInitialized() {
        return (gles != null);
    }

    @Override
    public void registerTypes(Type<?>[] types) {
        TypeResolver.getInstance().registerTypes(types);
    }

}
