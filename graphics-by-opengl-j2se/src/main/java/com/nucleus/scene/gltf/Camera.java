package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.vecmath.Matrix;

/**
 * A camera's projection. A node can reference a camera to apply a transform to place the camera in the scene.
 * 
 * Properties
 * 
 * Type Description Required
 * orthographic object An orthographic camera containing properties to create an orthographic projection matrix. No
 * perspective object A perspective camera containing properties to create a perspective projection matrix. No
 * type string Specifies if the camera uses a perspective or orthographic projection. ✅ Yes
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 */
public class Camera extends GLTFNamedValue {

    public enum Type {
        orthographic(),
        perspective();
    }

    private static final String ORTHOGRAPHIC = "orthographic";
    private static final String PERSPECTIVE = "perspective";
    private static final String TYPE = "type";

    /**
     * 
     * A perspective camera containing properties to create a perspective projection matrix.
     * 
     * Properties
     *
     * Type Description Required
     * aspectRatio number The floating-point aspect ratio of the field of view. No
     * yfov number The floating-point vertical field of view in radians. ✅ Yes
     * zfar number The floating-point distance to the far clipping plane. No
     * znear number The floating-point distance to the near clipping plane. ✅ Yes
     * extensions object Dictionary object with extension-specific objects. No
     * extras any Application-specific data. No
     *
     */
    public static class Perspective {

        private static final String ASPECT_RATIO = "aspectRatio";
        private static final String YFOV = "yfov";
        private static final String ZFAR = "zfar";
        private static final String ZNEAR = "znear";

        @SerializedName(ASPECT_RATIO)
        private float aspectRatio;
        @SerializedName(YFOV)
        private float yfov;
        @SerializedName(ZFAR)
        private float zfar = -1;
        @SerializedName(ZNEAR)
        private float znear;

        public Perspective() {

        }

        public Perspective(float aspectRatio, float yfov, float zfar, float znear) {
            this.aspectRatio = aspectRatio;
            this.yfov = yfov;
            this.zfar = zfar;
            this.znear = znear;
        }

        public Perspective(Perspective source) {
            this.aspectRatio = source.aspectRatio;
            this.yfov = source.yfov;
            this.zfar = source.zfar;
            this.znear = source.znear;
        }

        public float getAspectRatio() {
            return aspectRatio;
        }

        public float getYfov() {
            return yfov;
        }

        public float getZfar() {
            return zfar;
        }

        public float getZnear() {
            return znear;
        }

        public float[] calculateMatrix() {
            float[] projection = Matrix.setIdentity(Matrix.createMatrix(), 0);
            if (zfar == -1) {
                return calculateMatrixInfinite(projection);
            }
            return calculateMatrixFinite(projection);
        }

        protected float[] calculateMatrixInfinite(float[] projection) {
            projection[0] = (float) (1 / (aspectRatio * Math.tan((0.5f * yfov))));
            projection[5] = (float) (1 / (Math.tan(0.5f * yfov)));
            projection[10] = -1f;
            projection[11] = -2 * znear;
            projection[14] = -1f;
            projection[15] = 0;
            return projection;
        }

        protected float[] calculateMatrixFinite(float[] projection) {
            projection[0] = (float) (1 / (aspectRatio * Math.tan((0.5f * yfov))));
            projection[5] = (float) (1 / (Math.tan(0.5f * yfov)));
            projection[10] = (zfar + znear) / (znear - zfar);
            projection[11] = (2 * zfar * znear) / (znear - zfar);
            projection[14] = -1f;
            projection[15] = 0;
            return projection;
        }

    }

    /**
     * 
     * An orthographic camera containing properties to create an orthographic projection matrix.
     * 
     * Properties
     *
     * Type Description Required
     * xmag number The floating-point horizontal magnification of the view. ✅ Yes
     * ymag number The floating-point vertical magnification of the view. ✅ Yes
     * zfar number The floating-point distance to the far clipping plane. zfar must be greater than znear. ✅ Yes
     * znear number The floating-point distance to the near clipping plane. ✅ Yes
     * extensions object Dictionary object with extension-specific objects. No
     * extras any Application-specific data. No *
     */
    public class Orthographic {

        private static final String XMAG = "xmag";
        private static final String YMAG = "ymag";
        private static final String ZFAR = "zfar";
        private static final String ZNEAR = "znear";

        @SerializedName(XMAG)
        private float xmag;
        @SerializedName(YMAG)
        private float ymag;
        @SerializedName(ZFAR)
        private float zfar;
        @SerializedName(ZNEAR)
        private float znear;

        public Orthographic(Orthographic source) {
            xmag = source.xmag;
            ymag = source.ymag;
            zfar = source.zfar;
            znear = source.znear;
        }

        public float getXmag() {
            return xmag;
        }

        public float getYmag() {
            return ymag;
        }

        public float getZfar() {
            return zfar;
        }

        public float getZnear() {
            return znear;
        }

        public float[] calculateMatrix() {
            float[] projection = Matrix.setIdentity(Matrix.createMatrix(), 0);
            projection[0] = 1 / xmag;
            projection[5] = 1 / ymag;
            projection[10] = 2 / (znear - zfar);
            projection[14] = (zfar + znear) / (znear - zfar);
            projection[15] = 1;
            return projection;
        }

    }

    @SerializedName(PERSPECTIVE)
    private Perspective perspective;
    @SerializedName(ORTHOGRAPHIC)
    private Orthographic orthographic;
    @SerializedName(TYPE)
    private Type type;

    /**
     * Runtime reference to the node where this camera is
     */
    transient Node node;
    transient float[] projectionMatrix;
    transient float[] inverseMatrix = Matrix.createMatrix();
    transient float[] cameraMatrix = Matrix.createMatrix();

    public Camera() {
    }

    /**
     * Creates a runtime instance of a camera with the specified Node
     * 
     * @param source
     * @param node
     */
    public Camera(Camera source, Node node) {
        switch (source.type) {
            case perspective:
                create(new Perspective(source.perspective), node);
                break;
            case orthographic:
                create(new Orthographic(source.orthographic), node);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for type: " + type);
        }
    }

    protected void create(Perspective perspective, Node node) {
        setNode(node);
        type = Type.perspective;
        this.perspective = perspective;
    }

    /**
     * Internal method
     * 
     * @param orthographic
     * @param node
     * @throws IllegalArgumentException If node is null
     */
    protected void create(Orthographic orthographic, Node node) {
        setNode(node);
        type = Type.perspective;
        this.orthographic = orthographic;
    }

    /**
     * Internal method
     * 
     * @param node
     * @throws IllegalArgumentException If node is null
     */
    protected void setNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node is null");
        }
        this.node = node;
    }

    /**
     * Creates a runtime instance of a camera with the specified projection and Node
     * 
     * @param perspective
     * @param node
     * @throws IllegalArgumentException If node is null
     */
    public Camera(Perspective perspective, Node node) {
        create(perspective, node);
    }

    /**
     * Creates a runtime instance of a camera with the specified projection and Node
     * 
     * @param perspective
     * @param node
     * @throws IllegalArgumentException If node is null
     */
    public Camera(Orthographic orthographic, Node node) {
        create(orthographic, node);
    }

    public Perspective getPerspective() {
        return perspective;
    }

    public Orthographic getOrthographic() {
        return orthographic;
    }

    public Type getType() {
        return type;
    }

    /**
     * Returns the projectionmatrix for this camera, if it has not been calculated it is calculated now and returned.
     * Next call will return the calculated matrix.
     * 
     * @return
     */
    public float[] getProjectionMatrix() {
        if (projectionMatrix != null) {
            return projectionMatrix;
        }
        switch (type) {
            case orthographic:
                projectionMatrix = orthographic.calculateMatrix();
                break;
            case perspective:
                projectionMatrix = perspective.calculateMatrix();
                break;
            default:
                throw new IllegalArgumentException("Not implemented for type " + type);
        }
        return projectionMatrix;
    }

    /**
     * Multiplies the matrix with the inverse of the node matrix, stores result in this class inverse matrix and
     * returns.
     * Use this method to get the transform for positioning a camera
     * 
     * @param matrix Matrix to multiply camera node with, or null
     * @return The camera matrix
     */
    public float[] concatCameraMatrix(float[] matrix) {
        if (node == null) {
            throw new IllegalArgumentException("Runtime node is null in Camera - not using instanced camera?");
        }
        float[] parents = node.concatParentsMatrix();
        Matrix.invertM(node.inverseMatrix, 0, parents, 0);
        float[] inverse = node.inverseMatrix;
        // node.updateMatrix();
        // inverse = node.invertMatrix();
        if (matrix != null) {
            Matrix.mul4(matrix, inverse, inverseMatrix);
        } else {
            Matrix.copy(inverse, 0, inverseMatrix, 0);
        }
        return inverseMatrix;
    }

}
