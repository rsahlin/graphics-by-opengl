package com.nucleus.scene.gltf;

import com.nucleus.mmi.core.InputProcessor;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Vec2;

public class AlignedNodeTransform {

    private float[][] matrix = new float[3][16];
    private float[][] axis = new float[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
    private float[][] rotatedAxis = new float[3][3];
    /**
     * The result of axis rotation from input
     */
    private float[] rotatedAxisMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);

    private float[] moveScale;
    /**
     * The scale from the scene viewmatrix, needed when translating
     */
    private float[] viewScale = new float[3];

    private Scene target;

    public AlignedNodeTransform(Scene target, float[] moveScale) {
        this.moveScale = new float[] { moveScale[0], moveScale[1] };
        this.target = target;
    }

    public void setNodeTarget(Scene target) {
        this.target = target;
    }

    public void rotate(float[] move) {
        // Rotate y axis according to concat matrix.
        Matrix.mulVec3(rotatedAxisMatrix, axis[1], rotatedAxis[1]);
        // Y axis rotation - taken from X axis change
        Matrix.setRotateM(matrix[1], 0, -(move[0] * moveScale[0]) * 3.14f, -rotatedAxis[1][0], rotatedAxis[1][1],
                -rotatedAxis[1][2]);
        // Rotate x axis according to concat matrix.
        Matrix.mulVec3(rotatedAxisMatrix, axis[0], rotatedAxis[0]);
        // X axis rotation - taken from Y axis change
        Matrix.setRotateM(matrix[0], 0, (move[1] * moveScale[1]) * 3.14f, rotatedAxis[0][0], -rotatedAxis[0][1],
                -rotatedAxis[0][2]);

        float[] sceneMatrix = target.getSceneTransform().getMatrix();
        Matrix.mul4(rotatedAxisMatrix, matrix[0], sceneMatrix);
        Matrix.mul4(sceneMatrix, matrix[1], rotatedAxisMatrix);
        Matrix.copy(rotatedAxisMatrix, 0, sceneMatrix, 0);
    }

    public void scale(Vec2 zoom) {
        float z = 1 + (zoom.vector[Vec2.MAGNITUDE] * zoom.vector[Vec2.X])
                / InputProcessor.getInstance().getPointerScaleY();
        float[] sceneMatrix = target.getSceneTransform().getMatrix();
        Matrix.scaleM(sceneMatrix, 0, z, z, z);
        Matrix.copy(sceneMatrix, 0, rotatedAxisMatrix, 0);
    }

    public void translate(float[] move) {
        float[] sceneMatrix = target.getSceneTransform().getMatrix();
        Matrix.getScale(target.getViewMatrix(), viewScale);
        Matrix.translate(sceneMatrix, (move[0] * moveScale[0]) / viewScale[0],
                (move[1] * moveScale[1]) / viewScale[1], 0);
        Matrix.copy(sceneMatrix, 0, rotatedAxisMatrix, 0);
    }

    public void resetRotation() {
        Matrix.setIdentity(matrix[0], 0);
        Matrix.setIdentity(matrix[1], 0);
        Matrix.setIdentity(matrix[2], 0);
        Matrix.setIdentity(rotatedAxisMatrix, 0);
    }

}
