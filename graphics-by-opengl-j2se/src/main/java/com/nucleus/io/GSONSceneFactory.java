package com.nucleus.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.BaseSceneData;
import com.nucleus.scene.Node;
import com.nucleus.scene.SceneData;

/**
 * GSON Serialilzer for nucleus scenegraph.
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

    @Override
    public void setRenderer(NucleusRenderer renderer) {
        if (renderer == null) {
            throw new IllegalArgumentException(NULL_RENDERER_ERROR);
        }
        this.renderer = renderer;
    }

    @Override
    public Node importScene(String filename, String name) throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        InputStream is = loader.getResourceAsStream(filename);
        try {
            return importScene(loader.getResourceAsStream(filename), name);
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
    public Node importScene(InputStream is, String name) throws IOException {
        if (renderer == null) {
            throw new IllegalStateException(RENDERER_NOT_SET_ERROR);
        }
        if (is == null) {
            throw new IllegalArgumentException(NULL_PARAMETER_ERROR + "inputstream");
        }
        Reader reader = new InputStreamReader(is, "UTF-8");
        Gson gson = new GsonBuilder().create();
        SceneData scene = getSceneFromJson(gson, reader);
        Node node = createScene(scene, name);
        return node;
    }

    /**
     * Returns the correct implementation of SceneData class, subclasses will override
     * 
     * @param gson
     * @param reader
     * @param classT
     * @return
     */
    protected SceneData getSceneFromJson(Gson gson, Reader reader) {
        return gson.fromJson(reader, BaseSceneData.class);
    }

    /**
     * Creates a scene from scenedata and returns the root.
     * 
     * @param scene The scene data
     * @param id The id of the root node to create and return.
     * @return The create scene or null if node with matching id was not found.
     * @throws IOException
     */
    private Node createScene(SceneData scene, String id) throws IOException {
        Node n = scene.getInstanceNode().getNodeById(id);
        if (n == null) {
            return null;
        }
        Node root = new Node(id);
        createRoot(scene, n, root);
        return root;
    }

    /**
     * Creates a Node for the specified nodedata using the resources in the scene.
     * If type is specified then the data for this type is appended to the Node.
     * 
     * @param scene
     * @param n
     * @return the created node
     */
    private void createRoot(SceneData scene, Node n, Node parent) throws IOException {
        Node node = null;
        node = createNode(scene, n, parent);
        if (n.getChildren() == null) {
            return;
        }
        // Recursively create children
        for (Node nd : n.getChildren()) {
            createNode(scene, nd, node);
        }
    }

    /**
     * Creates a Node for the specified nodedata using the resources in the scene.
     * If type is specified then the data for this type is appended to the Node.
     * 
     * @param scene
     * @param nodedata
     * @param node
     * @return The created node
     */
    protected Node createNode(SceneData scene, Node nodeData, Node parent) throws IOException {
        return null;
    }

    /**
     * Checks if the node data has viewfrustum data, if it has it is set in the node.
     * 
     * @param source The source node containing the viewfrustum
     * @param node
     */
    protected void setViewFrustum(Node source, Node node) {
        ViewFrustum projection = source.getViewFrustum();
        if (projection == null) {
            return;
        }
        node.setProjection(projection.getMatrix());
    }

    @Override
    public void exportScene(OutputStream out, Object obj) throws IOException {
        if (!(obj instanceof Node)) {
            throw new IllegalArgumentException(WRONG_CLASS_ERROR + obj.getClass().getName());
        }
        Node node = (Node) obj;
        // First create the scenedata to hold resources and instances.
        // Subclasses may need to override to return correct instance of SceneData
        SceneData sceneData = createSceneData();
        sceneData.setInstanceNode(node);

    }

    /**
     * Creates the correct scenedata implementation, subclasses must implement this method as needed
     * 
     * @return The SceneData implementation to use for the Serializer
     */
    protected SceneData createSceneData() {
        return new BaseSceneData();
    }

}
