package com.nucleus.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.exporter.NodeExporter;
import com.nucleus.exporter.NucleusNodeExporter;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.BaseRootNode;
import com.nucleus.scene.DefaultNodeFactory;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeFactory;
import com.nucleus.scene.NodeType;
import com.nucleus.scene.RootNode;

/**
 * GSON Serializer for nucleus scenegraph.
 * 
 * @author Richard Sahlin
 *
 */
public class GSONSceneFactory implements SceneSerializer {

    private final static String ERROR_CLOSING_STREAM = "Error closing stream:";
    private final static String NULL_PARAMETER_ERROR = "Parameter is null: ";
    public final static String NOT_IMPLEMENTED = "Not implemented: ";
    private final static String WRONG_CLASS_ERROR = "Wrong class: ";

    protected NucleusRenderer renderer;
    protected NodeExporter nodeExporter;
    protected NodeFactory nodeFactory;

    /**
     * Creates a default scenefactory with {@link NucleusNodeExporter}.
     * Calls {@link #createNodeExporter()} and {@link #registerNodeExporters()}
     */
    public GSONSceneFactory() {
        createNodeExporter();
        registerNodeExporters();
    }

    @Override
    public void init(NucleusRenderer renderer, NodeFactory nodeFactory) {
        if (renderer == null) {
            throw new IllegalArgumentException(NULL_RENDERER_ERROR);
        }
        if (nodeFactory == null) {
            throw new IllegalArgumentException(NULL_NODEFACTORY_ERROR);
        }
        this.renderer = renderer;
        this.nodeFactory = nodeFactory;
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
        Gson gson = new GsonBuilder().create();
        RootNode scene = getSceneFromJson(gson, reader);
        return createScene(scene);
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
     * Creates a scene from scenedata and returns the root.
     * 
     * @param scene The scene data
     * @return The created scene or null if there is an error.
     * @throws IOException
     */
    private RootNode createScene(RootNode scene) throws IOException {
        ArrayList<LayerNode> source = scene.getScenes();
        if (source == null) {
            return null;
        }
        return createRoot(scene, source);
    }

    /**
     * Creates a Node for the specified nodedata using the resources in the scene.
     * If type is specified then the data for this type is appended to the Node.
     * 
     * @param scene
     * @param source
     * @return the created nodes
     */
    private RootNode createRoot(RootNode scene, ArrayList<LayerNode> source) throws IOException {
        RootNode root = createSceneData();
        for (LayerNode n : source) {
            root.addScene(createNode(scene, n));
        }
        return root;
    }

    private LayerNode createNode(RootNode scene, LayerNode source) throws IOException {
        LayerNode created = (LayerNode) nodeFactory.create(renderer, source, scene);
        setViewFrustum(source, created);
        createChildNodes(scene, source, created);
        return created;
    }

    /**
     * Creates a new node from the source node, looking up resources as needed.
     * The new node will be returned, it is not added to the parent node - this shall be done by the caller.
     * The new node will have parent as its parent node
     * 
     * @param scene
     * @param source
     * @param node
     * @return The created node
     */
    protected Node createNode(RootNode scene, Node source, Node parent) throws IOException {
        Node created = nodeFactory.create(renderer, source, scene);
        setViewFrustum(source, created);
        createChildNodes(scene, source, created);
        return created;

    }

    protected void createChildNodes(RootNode scene, Node source, Node parent) throws IOException {
        // Recursively create children
        for (Node nd : source.getChildren()) {
            Node child = createNode(scene, nd, parent);
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

}
