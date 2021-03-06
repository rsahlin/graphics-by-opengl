package com.nucleus.scene.gltf;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.SimpleLogger;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.scene.gltf.Camera.Perspective;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.vecmath.Matrix;

/**
 * The glTF asset contains zero or more scenes, the set of visual objects to render. Scenes are defined in a scenes
 * array. An additional property, scene (note singular), identifies which of the scenes in the array is to be displayed
 * at load time.
 * All nodes listed in scene.nodes array must be root nodes (see the next section for details).
 * When scene is undefined, runtime is not required to render anything at load time.
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

    public Scene() {

    }

    public Scene(GLTF asset, int[] nodes) {
        if (nodes != null) {
            this.nodes = new int[nodes.length];
            System.arraycopy(nodes, 0, this.nodes, 0, nodes.length);
            resolve(asset);
        }
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
     * Returns the first found node that has a mesh - will search recursively into each node in this scene
     * (depth first) - or null if no mesh found
     * 
     * @return First found node with Mesh, or null
     */
    public Node getFirstNodeWithMesh() {
        for (Node node : sceneNodes) {
            Node meshNode = node.getFirstNodeWithMesh();
            if (meshNode != null) {
                return meshNode;
            }
        }
        return null;
    }

    @Override
    public void resolve(GLTF asset) {
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
            // Default to selecting the first camera
            selectCameraInstance(getCameraInstanceCount() - 1);
        }
    }

    /**
     * Creates a default perspective projection camera, the camera will be added to list of cameras.
     * Camera will have a Node, camera index in node will be set.
     * 
     */
    protected void addDefaultCamera(GLTF gltf) {
        // Setup a default projection
        // Scale
        // Node meshNode = getFirstNodeWithMesh();
        // MaxMin maxMin = meshNode.calculateBounds();
        MaxMin maxMin = calculateBounds();
        float[] result = new float[3];
        float scale = maxMin.getMaxDelta(result)[1];
        if (scale == 0) {
            // Y is totally flat - use x
            scale = result[0] * (16f / 9);
        }
        if (scale == 0) {
            scale = 1.0f;
        }
        float YFOV = 0.8f;
        Perspective p = new Perspective(16 / 9f, YFOV, 1000, 0.1f);
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
            // SimpleLogger.d(getClass(), "Found mesh parent, using inverse matrix for default camera");
            // Go backwards in parent hierarchy to find inverse matrix
            // float[] matrix = parent.concatParentsMatrix();
            // Matrix.invertM(node.getMatrix(), 0, matrix, 0);
        }
        // Translation is applied to camera - so negate values, moving camera up will move scene down
        maxMin.getTranslateToCenter(result);
        Matrix.translate(node.getMatrix(), -result[0] / scale, -result[1] / scale, (-maxMin.maxmin[5] / scale) + 1);
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
     * Sets the view and projection matrix according to the chosen camera in the scene.
     * 
     * @param matrices View and projection matrices are set here
     * 
     */
    public void setMVP(float[][] matrices) {
        if (isCameraInstanced()) {
            Camera camera = getCameraInstance();
            camera.getProjectionMatrix(matrices[Matrices.PROJECTION.index], 0);
            camera.copyViewMatrix(matrices[Matrices.VIEW.index], 0);
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
     * Calculates the bounds values for the meshes (Accessors) in this scene, this will expand a boundingbox
     * with each transform node.
     * 
     * @return
     */
    public MaxMin calculateBounds() {
        if (sceneNodes != null) {
            MaxMin mm = new MaxMin();
            float[] matrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
            for (Node node : sceneNodes) {
                if (node != null) {
                    node.calculateBounds(mm, matrix);
                }
            }
            return mm;
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
