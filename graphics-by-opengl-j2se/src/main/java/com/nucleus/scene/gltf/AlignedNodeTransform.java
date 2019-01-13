package com.nucleus.scene.gltf;

import com.nucleus.mmi.core.CoreInput;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Vec2;
import com.nucleus.vecmath.Vec3;

/**
 * Used to transform a scene target. Rotation will be according to world axis so that this class
 * can be used in a ui to rotate according to input movement.
 *
 */
public class AlignedNodeTransform {

    private float[][] matrix = new float[2][16];
    private float[][] axisAngle = new float[][] { { 1, 0, 0, 0 }, { 0, 1, 0, 0 } };
    /**
     * The result of axis rotation from input - only store rotation here
     */
    private float[] rotationMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
    private float[] resultMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
    private float[] scale = new float[] { 1, 1, 1 };
    private float[] translate = new float[3];
    private float[] moveScale;
    /**
     * The scale from the scene viewmatrix, needed when translating
     */
    private float[] viewScale = new float[3];

    private Scene target;

    public AlignedNodeTransform(Scene target, float[] moveScale) {
        this.moveScale = new float[] { moveScale[0], moveScale[1], moveScale[2] };
        this.target = target;
        resetRotation();
    }

    public void setNodeTarget(Scene target) {
        this.target = target;
    }

    /**
     * Rotates the scene target according to movement values x and y, multiplied by the moveScale
     * This will rotate around the world axis, ie previous rotation is first applied then the current rotation is added.
     * 
     * 
     * @param move
     */
    public void rotate(float[] move) {
        axisAngle[1][3] = -(move[0] * moveScale[0]) * 3.14f;
        axisAngle[0][3] = (move[1] * moveScale[1]) * 3.14f;
        Matrix.setRotateM(matrix[0], 0, axisAngle[1][3], axisAngle[1][0], axisAngle[1][1], axisAngle[1][2]);
        Matrix.rotateM(matrix[0], axisAngle[0]);
        Matrix.mul4(matrix[0], matrix[1], rotationMatrix);
        System.arraycopy(rotationMatrix, 0, matrix[1], 0, Matrix.MATRIX_ELEMENTS);
        composeMatrix(target.getSceneTransform().getMatrix());
    }

    /**
     * Scales the scene
     * 
     * @param zoom
     */
    public void scale(Vec2 zoom) {
        float z = 1 + (zoom.vector[Vec2.MAGNITUDE] * zoom.vector[Vec2.X])
                / CoreInput.getInstance().getPointerScaleY();
        scale[0] *= z;
        scale[1] *= z;
        scale[2] *= z;
        composeMatrix(target.getSceneTransform().getMatrix());
    }

    /**
     * Translates the scene using the x,y and z, multiplied by {@link #moveScale} and divided by the scale in the
     * target matrix.
     * 
     * @param move
     */
    public void translate(float[] move) {
        Matrix.getScale(target.getViewMatrix(), viewScale);
        translate[0] += (move[0] * moveScale[0]) / viewScale[0];
        translate[1] += (move[1] * moveScale[1]) / viewScale[1];
        translate[2] += (move[2] * moveScale[2]) / viewScale[2];
        composeMatrix(target.getSceneTransform().getMatrix());
    }

    /**
     * Resets the rotation
     */
    public void resetRotation() {
        Matrix.setIdentity(matrix[0], 0);
        Matrix.setIdentity(matrix[1], 0);
        Matrix.setIdentity(rotationMatrix, 0);
        Vec3.set(scale, 1, 1, 1);
        Vec3.clear(translate);
    }

    private float[] composeMatrix(float[] destination) {
        Matrix.setIdentity(resultMatrix, 0);
        Matrix.scaleM(resultMatrix, 0, scale);
        Matrix.translate(resultMatrix, translate);
        Matrix.mul4(resultMatrix, rotationMatrix, destination);
        return destination;
    }

}
