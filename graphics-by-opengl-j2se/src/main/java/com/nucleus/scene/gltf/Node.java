package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.vecmath.Matrix;

/**
 * The Node as it is loaded using the glTF format.
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
public class Node extends GLTFNamedValue implements RuntimeResolver {

    private static final String MESH = "mesh";
    private static final String CHILDREN = "children";
    private static final String CAMERA = "camera";
    private static final String ROTATION = "rotation";
    private static final String SCALE = "scale";
    private static final String TRANSLATION = "translation";
    private static final String MATRIX = "matrix";

    @SerializedName(MESH)
    private int mesh = -1;
    @SerializedName(CHILDREN)
    private int[] children;
    @SerializedName(CAMERA)
    private int camera = -1;
    @SerializedName(ROTATION)
    private float[] rotation;
    @SerializedName(SCALE)
    private float[] scale;
    @SerializedName(TRANSLATION)
    private float[] translation;
    @SerializedName(MATRIX)
    private float[] matrix = Matrix.setIdentity(Matrix.createMatrix(), 0);

    transient protected Node[] childNodes;
    transient protected Mesh nodeMesh;
    transient protected Camera cameraRef;
    /**
     * The node concatenated model matrix at time of render, this is set when the node is rendered and
     * {@link #concatMatrix(float[])} is called
     * May be used when calculating bounds/collision on the current frame.
     * DO NOT WRITE TO THIS!
     */
    transient float[] modelMatrix = Matrix.createMatrix();
    /**
     * The node inverse matrix call {@link #invertMatrix()} to calculate.
     * May be used when calculating bounds/collision on the current frame.
     * DO NOT WRITE TO THIS!
     * 
     */
    transient float[] inverseMatrix = Matrix.createMatrix();

    /**
     * Returns the index of the mesh to render with this node
     * 
     * @return
     */
    public int getMeshIndex() {
        return mesh;
    }

    /**
     * Returns the mesh to render with this node - or null if not defined.
     * 
     * @return
     */
    public Mesh getMesh() {
        return nodeMesh;
    }

    /**
     * Returns the array of indexes that are the childnodes of this node
     * 
     * @return
     */
    public int[] getChildIndexes() {
        return children;
    }

    /**
     * Returns the childnodes
     * 
     * @return
     */
    public Node[] getChildren() {
        return childNodes;
    }

    /**
     * Returns the camera index
     * 
     * @return
     */
    public int getCameraIndex() {
        return camera;
    }

    /**
     * Returns the camera for this node or null if not defined.
     * 
     * @return
     */
    public Camera getCamera() {
        return cameraRef;
    }

    public float[] getRotation() {
        return rotation;
    }

    public float[] getScale() {
        return scale;
    }

    public float[] getTranslation() {
        return translation;
    }

    /**
     * If RTS values are defined the matrix is set according to these.
     * Otherwise matrix is left unchanged and returned
     * 
     * @return This nodes matrix, with updated TRS if used.
     */
    protected float[] updateMatrix() {
        if (rotation != null || scale != null || translation != null) {
            Matrix.setIdentity(matrix, 0);
            Matrix.rotateM(matrix, rotation);
            Matrix.scaleM(matrix, 0, scale);
            Matrix.translate(matrix, translation);
        }
        return matrix;
    }

    public float[] invertMatrix() {
        Matrix.invertM(inverseMatrix, 0, matrix, 0);
        return inverseMatrix;
    }

    /**
     * Multiply the matrix with this nodes transform/matrix and store in this nodes model matrix.
     * If this node does not have a transform an identity matrix is used.
     * 
     * @param concatModel The matrix to multiply with this nodes transform/matrix
     * @return The node model matrix - this nodes transform * matrix - this is a reference to the concatenated
     * model matrix in this class
     */
    public float[] concatMatrix(float[] matrix) {
        if (matrix == null) {
            return updateMatrix();
        }
        Matrix.mul4(matrix, updateMatrix(), modelMatrix);
        return modelMatrix;
    }

    /**
     * Returns the Matrix as an array of floats - this is the current matrix.
     * 
     * @return
     */
    public float[] getMatrix() {
        return matrix;
    }

    @Override
    public void resolve(GLTF asset) throws GLTFException {
        if (matrix != null) {
            float[] transpose = Matrix.createMatrix(matrix);
            Matrix.transposeM(matrix, 0, transpose, 0);
        }
        if (mesh >= 0) {
            nodeMesh = asset.getMeshes()[mesh];
        }
        if (children != null && children.length > 0) {
            Node[] sources = asset.getNodes();
            childNodes = new Node[children.length];
            for (int i = 0; i < children.length; i++) {
                childNodes[i] = sources[children[i]];
            }
        }
        if (camera != -1) {
            cameraRef = asset.getCameras()[camera];
        }

    }

    /**
     * Returns the non transformed POSITION bounding (max - min) values (three component) for the geometry in this node
     * and children.
     * This will search through all primitives used by the node and return the non transformed bound (max - min) values.
     * 
     * @return
     */
    public float[] getPositionBounds() {
        float[] result = new float[3];
        getPositionScale(result);
        return result;
    }

    /**
     * Returns the non transformedPOSITION bounding (max - min) (three component) for the geometry in the list of nodes.
     * This will search through all primitives used by the nodes and return the non transformed bound (max - min)
     * values.
     * 
     * @param nodes List of nodes to include, children will be called as well.
     * @param compare Current max values that will be updated
     */
    public static void getPositionScale(Node[] nodes, float[] result) {
        if (nodes != null) {
            for (Node n : nodes) {
                n.getPositionScale(result);
            }
        }
    }

    /**
     * Returns the non transformed POSITION bounding (max - min) (three component) for the geometry in this node and
     * children.
     * This will search through all primitives used by the node and return the non transformed bound (max - min) values.
     * 
     * @param compare Current max values that will be updated
     */
    public void getPositionScale(float[] compare) {
        if (getMesh() != null && getMesh().getPrimitives() != null) {
            for (Primitive p : getMesh().getPrimitives()) {
                if (p.getAttributesArray() != null) {
                    Accessor accessor = p.getAccessor(Attributes.POSITION);
                    if (accessor != null) {
                        accessor.getBoundsScale(compare, compare);
                    }
                }
            }
        }
        getPositionScale(getChildren(), compare);
    }

}
