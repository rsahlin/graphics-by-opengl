package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Matrix.MatrixStack;

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
 * For Version 2.0 conformance, the glTF node hierarchy is not a directed acyclic graph (DAG) or scene graph,
 * but a disjoint union of strict trees.
 * That is, no node may be a direct descendant of more than one node.
 * This restriction is meant to simplify implementation and facilitate conformance.
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
    transient protected Node parent;
    /**
     * The node concatenated model matrix at time of render, this is set when the node is rendered and
     * {@link #concatMatrix(float[])} is called
     * May be used when calculating bounds/collision on the current frame.
     * DO NOT WRITE TO THIS!
     */
    transient float[] modelMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
    /**
     * The node inverse matrix call {@link #invertMatrix()} to calculate.
     * May be used when calculating bounds/collision on the current frame.
     * DO NOT WRITE TO THIS!
     * 
     */
    transient float[] inverseMatrix = Matrix.createMatrix();

    /**
     * Used if the parents hierarchy transform shall be calculated
     */
    transient float[] parentMatrix = Matrix.createMatrix();

    public Node() {
    }

    /**
     * Creates a camera node - only use this for default camera
     * 
     * @param camera
     * @param cameraIndex
     */
    public Node(Camera camera, int cameraIndex) {
        cameraRef = camera;
        this.camera = cameraIndex;
    }

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
     * If this node has a mesh, then this node is returned, otherwise children are searched (depth first) for
     * the first node containing a mesh
     * 
     * @return Node containing mesh, or null
     */
    public Node getFirstNodeWithMesh() {
        if (getMesh() != null) {
            return this;
        }
        if (childNodes == null) {
            return null;
        }
        for (Node child : childNodes) {
            Node meshNode = child.getFirstNodeWithMesh();
            if (meshNode != null) {
                return meshNode;
            }
        }
        return null;
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
     * Returns true if this Node has defined rotation, translation or scale value
     * If true then Matrix is not used.
     * 
     * @return
     */
    public final boolean hasRTS() {
        return (rotation != null || scale != null || translation != null);
    }

    /**
     * If RTS values are defined the matrix is set according to these.
     * Otherwise matrix is left unchanged and returned
     * 
     * @return This nodes matrix, with updated TRS if used.
     */
    protected float[] updateMatrix() {
        if (hasRTS()) {
            Matrix.setIdentity(matrix, 0);
            Matrix.setQuaternionRotation(rotation, matrix);
            Matrix.translate(matrix, translation);
            Matrix.scaleM(matrix, 0, scale);
        }
        return matrix;
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
    public void resolve(GLTF gltf) throws GLTFException {
        if (matrix != null) {
            float[] transpose = Matrix.createMatrix(matrix);
            Matrix.transposeM(matrix, 0, transpose, 0);
        }
        if (mesh >= 0) {
            nodeMesh = gltf.getMeshes()[mesh];
        }
        if (children != null && children.length > 0) {
            Node[] sources = gltf.getNodes();
            childNodes = new Node[children.length];
            for (int i = 0; i < children.length; i++) {
                childNodes[i] = sources[children[i]];
                if (childNodes[i].parent != null) {
                    throw new IllegalArgumentException("Child already has parent - duplicate reference?");
                }
                childNodes[i].parent = this;
            }
        }
        setCamera(gltf, camera);
    }

    /**
     * Attaches or removes a camera from this node, specify -1 to remove camera
     * 
     * @param gltf
     * @param camera
     */
    public void setCamera(GLTF gltf, int camera) {
        this.camera = camera;
        cameraRef = gltf.getCamera(camera);
    }

    /**
     * Returns the parent node or null if this node is root
     * 
     * @return
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Returns the transformed POSITION bounding (max - min) (three component) for the geometry in this node and
     * children.
     * This will search through all primitives used by the node and return the transformed bound (max - min) values.
     * Use this to get the bounds volume for enclosed (transformed) geometry in nodes.
     * 
     * @param bounds Current bounds values that will be updated
     * @param matrix The current transform
     * @return The updated bounds
     */
    public MaxMin calculateBounds(MaxMin bounds, float[] matrix) {
        return calculateBounds(bounds, matrix, new Matrix.MatrixStack());
    }

    /**
     * Same as {@link #calculateBounds(MaxMin, float[])} but using supplied {@link MatrixStack}
     * 
     * @param bounds The bounds to update
     * @param matrix Current matrix
     * @param stack Matrix stack
     * @return The updated bounds
     */
    public MaxMin calculateBounds(MaxMin bounds, float[] matrix, MatrixStack stack) {
        if (matrix != null) {
            stack.push(matrix, 0);
            float[] concatMatrix = Matrix.createMatrix();
            Matrix.mul4(matrix, updateMatrix(), concatMatrix);
            System.arraycopy(concatMatrix, 0, matrix, 0, Matrix.MATRIX_ELEMENTS);
        } else {
            matrix = Matrix.createMatrix();
            stack.push(matrix, 0);
            System.arraycopy(updateMatrix(), 0, matrix, 0, Matrix.MATRIX_ELEMENTS);
        }
        if (getMesh() != null && getMesh().getPrimitives() != null) {
            for (Primitive p : getMesh().getPrimitives()) {
                if (p.getAttributesArray() != null) {
                    Accessor accessor = p.getAccessor(Attributes.POSITION);
                    if (accessor != null) {
                        MaxMin maxMin = new MaxMin(accessor.getMax(), accessor.getMin());
                        bounds.update(maxMin, matrix);
                    }
                }
            }
        }
        if (childNodes != null) {
            for (Node child : childNodes) {
                child.calculateBounds(bounds, matrix, stack);
            }
        }
        stack.pop(matrix, 0);
        return bounds;
    }

    /**
     * Calculate the bounds, starting from this node
     * 
     * @return
     */
    public MaxMin calculateBounds() {
        return calculateBounds(new MaxMin(), null);
    }

    @Override
    public String toString() {
        String str = "";
        if (getMesh() != null && getMesh().getPrimitives() != null
                && getMesh().getPrimitives()[0].getMaterial() != null) {
            PBRMetallicRoughness pbr = getMesh().getPrimitives()[0].getMaterial().getPbrMetallicRoughness();
            if (pbr != null && pbr.getBaseColorFactor() != null) {
                float[] color = pbr.getBaseColorFactor();
                str = "Color: " + color[0] + ", " + color[1] + ", " + color[2] + ", ";
            }
        }
        return str + (name != null ? ("name: " + name + ", ")
                : "" + children != null ? children.length + " children, "
                        : "" + rotation != null
                                ? "rotate: " + rotation[0] + ", " + rotation[1] + ", " + rotation[2] + ", "
                                        + rotation[3]
                                : "");
    }

    /**
     * Calculates the current transform, by going through parents transforms.
     * Used for instance by camera to find the result matrix.
     * 
     * @return the result matrix
     */
    public float[] concatParentsMatrix() {
        if (parent != null) {
            Matrix.mul4(parent.updateMatrix(), updateMatrix(), parentMatrix);
            return parent.concatParentsMatrix(parentMatrix);
        }
        return Matrix.copy(matrix, 0, parentMatrix, 0);
    }

    /**
     * Calculates the current transform, by going through parents transforms.
     * Used for instance by camera to find the result matrix.
     * 
     * @param The current matrix
     * @return result The sum of this nodes transform and all direct parents.
     */
    private float[] concatParentsMatrix(float[] matrix) {
        if (parent != null) {
            Matrix.mul4(parent.matrix, matrix, parentMatrix);
            return parent.concatParentsMatrix(parentMatrix);
        }
        return matrix;
    }

    /**
     * If rotation is present it is cleared to 0
     */
    private final void clearRotation() {
        if (rotation != null) {
            rotation[0] = 0;
            rotation[1] = 0;
            rotation[2] = 0;
            rotation[3] = 0;
        }
    }

    /**
     * If translation is present it is cleared to 0
     */
    private final void clearTranslation() {
        if (translation != null) {
            translation[0] = 0;
            translation[1] = 0;
            translation[2] = 0;
        }
    }

    /**
     * If scale is present it is cleared to 1
     */
    private final void clearScale() {
        if (scale != null) {
            scale[0] = 1;
            scale[1] = 1;
            scale[2] = 1;
        }
    }

    /**
     * Clears all transform values - if Matrix is used it is set to identity.
     * Any RTS values are cleared and scale set to 1,1,1 if present.
     */
    public void clearTransform() {
        if (hasRTS()) {
            clearRotation();
            clearTranslation();
            clearScale();
            updateMatrix();
        } else {
            Matrix.setIdentity(matrix, 0);
            Matrix.setIdentity(modelMatrix, 0);
        }
    }

}
