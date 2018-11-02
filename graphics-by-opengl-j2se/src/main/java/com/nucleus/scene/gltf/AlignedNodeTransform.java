package com.nucleus.scene.gltf;

import com.nucleus.SimpleLogger;
import com.nucleus.common.StringUtils;
import com.nucleus.mmi.core.InputProcessor;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Vec2;
import com.nucleus.vecmath.Vec3;

public class AlignedNodeTransform {

    private float[][] matrix = new float[3][16];
    private float[][] axis = new float[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
    private float[][] rotatedAxis = new float[3][3];
    /**
     * The result of axis rotation from input - only store rotation here
     */
    private float[] rotationMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
    private float[] resultMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
    private float[] concatMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
    private float[] scale = new float[] { 1, 1, 1 };
    private float[] translate = new float[3];
    private float[] moveScale;
    /**
     * The scale from the scene viewmatrix, needed when translating
     */
    private float[] viewScale = new float[3];

    private Scene target;

    public AlignedNodeTransform(Scene target, float[] moveScale) {
        this.moveScale = new float[] { moveScale[0], moveScale[1] };
        this.target = target;
        resetRotation();
    }

    public void setNodeTarget(Scene target) {
        this.target = target;
    }

    public void rotate(float[] move) {
        // Rotate y axis according to x rotation
        Matrix.mulVec3(matrix[0], axis[1], rotatedAxis[1]);
        // Y axis rotation - taken from X axis change
        SimpleLogger.d(getClass(), "X axis move, Y axis is: " + StringUtils.getString(rotatedAxis[1]) + " : " + move[0] * moveScale[0]);
        Matrix.rotateM(matrix[0], new float[] {rotatedAxis[1][0], rotatedAxis[1][1],
                -rotatedAxis[1][2], -(move[0] * moveScale[0]) * 3.14f});
        // Rotate x axis according to y rotation
        Matrix.mulVec3(matrix[0], axis[0], rotatedAxis[0]);
        // X axis rotation - taken from Y axis change
        SimpleLogger.d(getClass(), "Y axis move, X axis is: " + StringUtils.getString(rotatedAxis[0]));
        Matrix.rotateM(matrix[0], new float[] {rotatedAxis[0][0], -rotatedAxis[0][1],
                -rotatedAxis[0][2], (move[1] * moveScale[1]) * 3.14f});
//        Matrix.mul4(matrix[1], matrix[0], rotationMatrix);
        System.arraycopy(matrix[0], 0, rotationMatrix, 0, 16);
        composeMatrix(target.getSceneTransform().getMatrix());
    }

    public void scale(Vec2 zoom) {
        float z = 1 + (zoom.vector[Vec2.MAGNITUDE] * zoom.vector[Vec2.X])
                / InputProcessor.getInstance().getPointerScaleY();
        scale[0] *= z;
        scale[1] *= z;
        scale[2] *= z;
        composeMatrix(target.getSceneTransform().getMatrix());
        // float[] sceneMatrix = target.getSceneTransform().getMatrix();
        // Matrix.scaleM(sceneMatrix, 0, z, z, z);
    }

    public void translate(float[] move) {
        float[] sceneMatrix = target.getSceneTransform().getMatrix();
        Matrix.getScale(target.getViewMatrix(), viewScale);
        translate[0] += (move[0] * moveScale[0]) / viewScale[0];
        translate[1] += (move[1] * moveScale[1]) / viewScale[1];
        composeMatrix(target.getSceneTransform().getMatrix());
        // Matrix.setTranslate(sceneMatrix, translate);
    }

    public void resetRotation() {
        Matrix.setIdentity(matrix[0], 0);
        Matrix.setIdentity(matrix[1], 0);
        Matrix.setIdentity(matrix[2], 0);
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
