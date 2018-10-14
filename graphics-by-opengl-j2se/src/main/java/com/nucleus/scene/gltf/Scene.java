package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.SimpleLogger;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;

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
    transient Node[] cameraNodes;
    /**
     * True if one or moer of the nodes reference a camera
     */
    transient boolean cameraDefined = false;

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
     * The array will match the size of {@link #getNodes()} and will contain a reference to a Node with a camera
     * if one is defind in that Node tree.
     * Use this to figure out on what nodes in the Scene to use a camera for.
     * 
     * @return
     */
    public Node[] getCameraNodes() {
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
            // Check for nodes that reference camera
            cameraNodes = new Node[sceneNodes.length];
            for (int i = 0; i < cameraNodes.length; i++) {
                cameraNodes[i] = asset.getCameraNode(sceneNodes[i]);
                if (cameraNodes[i] != null) {
                    cameraDefined = true;
                    SimpleLogger.d(getClass(),
                            "Found camera in scene " + getName() + ", for node index " + i + ", name: "
                                    + sceneNodes[i].getName());
                }
            }
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

}
