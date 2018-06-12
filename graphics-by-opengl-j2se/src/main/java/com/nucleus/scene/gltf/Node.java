package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * node
 * A node in the node hierarchy. When the node contains skin, all mesh.primitives must contain JOINTS_0 and WEIGHTS_0
 * attributes. A node can have either a matrix or any combination of translation/rotation/scale (TRS) properties. TRS
 * properties are converted to matrices and postmultiplied in the T * R * S order to compose the transformation matrix;
 * first the scale is applied to the vertices, then the rotation, and then the translation. If none are provided, the
 * transform is the identity. When a node is targeted for animation (referenced by an animation.channel.target), only
 * TRS properties may be present; matrix will not be present.
 * 
 * Properties
 * 
 * Type Description Required
 * camera integer The index of the camera referenced by this node. No
 * children integer [1-*] The indices of this node's children. No
 * skin integer The index of the skin referenced by this node. No
 * matrix number [16] A floating-point 4x4 transformation matrix stored in column-major order. No, default:
 * [1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1]
 * mesh integer The index of the mesh in this node. No
 * rotation number [4] The node's unit quaternion rotation in the order (x, y, z, w), where w is the scalar. No,
 * default: [0,0,0,1]
 * scale number [3] The node's non-uniform scale, given as the scaling factors along the x, y, and z axes. No, default:
 * [1,1,1]
 * translation number [3] The node's translation along the x, y, and z axes. No, default: [0,0,0]
 * weights number [1-*] The weights of the instantiated Morph Target. Number of elements must match number of Morph
 * Targets of used mesh. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 */
public class Node {

    private static final String NAME = "name";
    private static final String MESH = "mesh";
    private static final String CHILDREN = "children";
    private static final String CAMERA = "camera";
    private static final String ROTATION = "rotation";
    private static final String SCALE = "scale";
    private static final String TRANSLATION = "translation";
    private static final String MATRIX = "matrix";

    @SerializedName(NAME)
    private String name;
    @SerializedName(MESH)
    private int mesh;
    @SerializedName(CHILDREN)
    private int[] children;
    @SerializedName(CAMERA)
    private int camera;
    @SerializedName(ROTATION)
    private int[] rotation;
    @SerializedName(SCALE)
    private int[] scale;
    @SerializedName(TRANSLATION)
    private int[] translation;
    @SerializedName(MATRIX)
    private float[] matrix;

    public String getName() {
        return name;
    }

    public int getMesh() {
        return mesh;
    }

    public int[] getChildren() {
        return children;
    }

    public int getCamera() {
        return camera;
    }

    public int[] getRotation() {
        return rotation;
    }

    public int[] getScale() {
        return scale;
    }

    public int[] getTranslation() {
        return translation;
    }

    public float[] getMatrix() {
        return matrix;
    }

}
