package com.nucleus.scene.gltf;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.SimpleLogger;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.scene.gltf.Camera.Perspective;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.vecmath.Matrix;

/**
 * The Scene as it is loaded using the glTF format.
 * 
 * Scenes use instances of Camera. The cameras that are referenced in this scene are stored here.
 * Use {@link #getCameraInstanceCount()} to check number of cameras. When scene is resolved a default camera will be
 * added, there shall be at least one camera in every scene.
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
    /**
     * The cameras that are referenced in this scene - this can be seen as the instanced cameras.
     */
    transient private ArrayList<Camera> instanceCameras = new ArrayList<>();
    /**
     * RTS or Matrix transform for the scene.
     * This can be used to transform just all nodes in the scene - minus camera (view)
     */
    transient private Node transform = new Node();
    transient private float[] viewMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
    transient private int selectedCamera = 0;

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
                    instanceCameras.add(new Camera(cameraNode.getCamera(), cameraNode));
                    SimpleLogger.d(getClass(),
                            "Found camera in scene " + getName() + ", for node index " + i + ", name: "
                                    + sceneNodes[i].getName());
                }
            }
            SimpleLogger.d(getClass(), "Found " + instanceCameras.size() + " cameras in scene. Adding default camera");
            addDefaultCamera(asset);
        }
    }

    /**
     * Creates a default perspective projection camera, the camera will be added to list of cameras.
     * Camera will have a Node, camera index in node will be set.
     * TODO - check bounds of geometry and create perspective accordingly
     * 
     */
    protected void addDefaultCamera(GLTF gltf) {
        // Setup a default projection
        // Scale
        MaxMin maxMin = calculateMaxMin();
        float[] delta = new float[2];
        maxMin.getMaxDeltaXY(delta);

        float YASPECT = 1f;
        Perspective p = new Perspective(16 / 9f, YASPECT, 10000, 0.1f);
        // This node will only be referenced by the camera
        // TODO - Should a special CameraNode be created? Maybe better to keep Nodes simple?
        Node node = new Node();
        Camera camera = new Camera(p, node);
        selectedCamera = instanceCameras.size();
        instanceCameras.add(camera);
        // Add camera to gltf
        int index = gltf.addCamera(camera);
        node.setCamera(gltf, index);

        Node parent = getMeshParent();
        // If parent to node with mesh is at root (ie does not have parent) then treat as normal
        if (parent != null && parent.getParent() != null) {
            SimpleLogger.d(getClass(), "Found mesh parent, using inverse matrix for default camera");
            // Go backwards in parent hierarchy to find inverse matrix
            float[] matrix = parent.concatParentsMatrix();
            Matrix.invertM(node.getMatrix(), 0, matrix, 0);
        }
        // For now just scale according to height.
        // The inverse of the node matrix will be used - so just set the largest values
        float scale = delta[1];
        Matrix.translate(node.getMatrix(), 0, 0, 1f);
        Matrix.scaleM(node.getMatrix(), 0, scale, scale, scale);
    }

    /**
     * Returns true if one or more of the nodes in this scene reference a camera, ie one or more cameras are instanced.
     * 
     * @return true If one or more cameras are instanced in this scene
     */
    public boolean isCameraInstanced() {
        return instanceCameras != null && instanceCameras.size() > 0;
    }

    /**
     * Returns the view (camera) matrix, if no camera is referenced this will be identity matrix
     * This matrix is set when {@link #setView(float[][], float[])} is called.
     * 
     * @return The view matrix as set by {@link #setView(float[][], float[])}
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
     * Sets the view and projection matrices according to the chosen camera in the scene.
     * 
     * @param matrices Matrix to set the view and projection to
     * 
     */
    public void setViewProjection(float[][] matrices) {
        if (isCameraInstanced()) {
            Camera camera = getCameraInstance();
            matrices[Matrices.PROJECTION.index] = camera.getProjectionMatrix();
            setView(camera, matrices);
        }
    }

    /**
     * Returns the chosen camera instance, ie the currently chosen camera instance in this scene.
     * When a scene is loaded the added camera is default.
     * 
     * @return The chosen camera instance, or null
     */
    public Camera getCameraInstance() {
        if (isCameraInstanced() && selectedCamera >= 0 && selectedCamera < instanceCameras.size()) {
            return instanceCameras.get(selectedCamera);
        }
        return null;
    }

    /**
     * Returns the index (of the cameras referenced in this scene) of the selected camera, used in
     * {@link #selectCameraInstance(int)}
     * 
     * @return Index of the currently selected camera, 0 - {@link #getCameraInstanceCount()}
     */
    public int getSelectedCameraIndex() {
        return selectedCamera;
    }

    /**
     * Returns the number of cameras defined in the scene, including default camera
     * 
     * @return Number of cameras in scene
     */
    public int getCameraInstanceCount() {
        return instanceCameras.size();
    }

    /**
     * Selects a camera to be used in this scene, the index is one of the cameras used in this scene - not same
     * as the total array of Cameras in the gltf asset.
     * If index < 0 or >= {@link #getCameraInstanceCount()} then nothing is done.
     * 
     * @param index Index of camera, 0 - {@link #getCameraInstanceCount()}
     */
    public void selectCameraInstance(int index) {
        if (index >= 0 && index < instanceCameras.size()) {
            selectedCamera = index;
        } else {
            SimpleLogger.d(getClass(),
                    "Invalid camera index: " + index + ", number of cameras: " + getCameraInstanceCount());
        }
    }

    /**
     * Selects the next camera, selecting the first camera if last camera is reached.
     * 
     * @return The index of the selected camera
     */
    public int selectNextCameraInstance() {
        selectedCamera++;
        if (selectedCamera >= instanceCameras.size()) {
            selectedCamera = 0;
        }
        return selectedCamera;
    }

    /**
     * Sets the view matrix according to camera (inverted)
     * This will locate the scene according to camera.
     * The result is stored in matrices as well as the local viewMatrix
     * Internal method - do not call directly
     * 
     * @param camera
     * @param matrices The matrix to set the view transform to, if camera is present. Otherwise nothing is done.
     */
    protected void setView(Camera camera, float[][] matrices) {
        matrices[Matrices.VIEW.index] = camera.concatCameraMatrix(null);
        Matrix.copy(matrices[Matrices.VIEW.index], 0, viewMatrix, 0);
    }

    /**
     * Calculates the max/min values for the meshes (Accessors) in this scene
     * same as calling {@link #calculateMaxMin(float[])} with scale set to 1,1,1
     * 
     * @return
     */
    public MaxMin calculateMaxMin() {
        return calculateMaxMin(new float[] { 1, 1, 1 });
    }

    /**
     * Calculates the max/min values for the meshes (Accessors) in this scene, scale is used as a first scale-factor.
     * 
     * @return
     */
    public MaxMin calculateMaxMin(float[] scale) {
        if (sceneNodes != null) {
            MaxMin mm = new MaxMin();
            return Node.updateMaxMin(sceneNodes, mm, scale);
        }
        return null;
    }

    /**
     * Returns the parent node to the first found mesh node. This can be used to locate the origin of the visible
     * scene.
     * Returns null if first Node containing a Mesh is at the root.
     * 
     * @return Parent to node hierarchy where first Mesh is, or null
     */
    public Node getMeshParent() {
        return getMeshParent(sceneNodes);
    }

    private Node getMeshParent(Node[] nodes) {
        if (nodes != null) {
            for (Node node : nodes) {
                if (node.nodeMesh != null) {
                    return node.parent;
                }
            }
            for (Node node : nodes) {
                Node parent = getMeshParent(node.childNodes);
                if (parent != null) {
                    return parent;
                }
            }
        }
        return null;
    }

    /**
     * Resets the scene Node transform, clearing any transform in the scene transform node.
     */
    public void clearSceneTransform() {
        transform.clearTransform();
    }

}
