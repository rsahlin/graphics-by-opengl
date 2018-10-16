package com.nucleus.scene.gltf;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.SimpleLogger;
import com.nucleus.scene.gltf.Camera.Perspective;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.vecmath.Matrix;

/**
 * The Scene as it is loaded using the glTF format.
 * 
 * scene
 * The root nodes of a scene.
 * 
 * Properties
 * 
 * Type Description Required
 * nodes integer [1-*] The indices of each root node. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 */
public class Scene extends GLTFNamedValue implements RuntimeResolver {

    private static final String NODES = "nodes";

    @SerializedName(NODES)
    private int[] nodes;

    /**
     * The nodes that make up this scene.
     */
    transient Node[] sceneNodes;
    transient ArrayList<Node> cameraNodes = new ArrayList<>();
    /**
     * True if one or more of the nodes reference a camera
     */
    transient boolean cameraDefined = false;
    /**
     * RTS or Matrix transform for the scene.
     * This can be used to transform just all nodes in the scene - minus camera (view)
     */
    transient Node transform = new Node();
    transient float[] viewMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
    transient int selectedCamera = 0;

    /**
     * returns the nodes, as indexes, that make up this scene
     */
    public int[] getNodeIndexes() {
        return nodes;
    }

    /**
     * Returns the nodes that make up this scene
     * 
     * @return
     */
    public Node[] getNodes() {
        return sceneNodes;
    }

    /**
     * Returns the nodes in the scene that defines a camera.
     * 
     * @return
     */
    public ArrayList<Node> getCameraNodes() {
        return cameraNodes;
    }

    @Override
    public void resolve(GLTF asset) throws GLTFException {
        if (nodes != null && nodes.length > 0) {
            Node[] sources = asset.getNodes();
            sceneNodes = new Node[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                sceneNodes[i] = sources[nodes[i]];
            }
            for (int i = 0; i < sceneNodes.length; i++) {
                Node cameraNode = asset.getCameraNode(sceneNodes[i]);
                if (cameraNode != null) {
                    cameraNodes.add(cameraNode);
                    cameraDefined = true;
                    SimpleLogger.d(getClass(),
                            "Found camera in scene " + getName() + ", for node index " + i + ", name: "
                                    + sceneNodes[i].getName());
                }
            }
            SimpleLogger.d(getClass(), "Found " + cameraNodes.size() + " camera nodes in scene.");
        }
    }

    /**
     * Returns true if one or more of the nodes in this scene reference a camera
     * 
     * @return true If one or more of the nodes reference a camera
     */
    public boolean isCameraDefined() {
        return cameraDefined;
    }

    /**
     * Returns the view (camera) matrix, if no camera is referenced this will be identity matrix
     * 
     * @return
     */
    public float[] getViewMatrix() {
        return viewMatrix;
    }

    /**
     * Returns the node holding the transform for the scene
     * 
     * @return
     */
    public Node getSceneTransform() {
        return transform;
    }

    /**
     * Sets the projection matrix if a camera is referenced in this scene - by default the first camera is chosen.
     * If no camera is referenced the a default projection is created.
     * 
     * @param matrix Matrix to set the projection to
     */
    public void setProjection(float[] matrix) {
        if (!isCameraDefined()) {
            // Setup a default projection if none is specified in model - this is to get the right axes and winding.
            Perspective p = new Perspective(1.5f, 0.66f, 10000, 1);
            Matrix.copy(p.calculateMatrix(), 0, matrix, 0);
        } else {
            // For now choose first used camera.
            Node cameraNode = getCameraNode();
            Matrix.copy(cameraNode.getCamera().getProjectionMatrix(), 0, matrix, 0);
        }
    }

    /**
     * Returns the default or chosen camera node, or null if none is referenced.
     * 
     * @return Node referencing a camera or null.
     */
    public Node getCameraNode() {
        if (isCameraDefined()) {
            return getCameraNodes().get(selectedCamera);
        }
        return null;
    }

    /**
     * Sets the view matrix according to camera (inverted) and modelMatrix if this scene references a camera - by
     * default
     * the first camera is chosen.
     * This will locate the scene according to camera and modelMatrix
     * 
     * @param viewMatrix The matrix to set the view transform to, if camera is present. Otherwise nothing is done.
     * @param modelMatrix The current modelMatrix
     */
    public void setView(float[] viewMatrix, float[] modelMatrix) {
        if (isCameraDefined()) {
            Node cameraNode = getCameraNode();
            Matrix.copy(cameraNode.getCamera().concatCameraMatrix(cameraNode, modelMatrix), 0, viewMatrix, 0);
        }

    }

}
