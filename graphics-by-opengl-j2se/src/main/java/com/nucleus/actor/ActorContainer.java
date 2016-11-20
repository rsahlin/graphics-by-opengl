package com.nucleus.actor;

import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

/**
 * Use System - Component instead, see {@linkplain SpriteS }
 * For objects containing actor logic
 * 
 * @author Richard Sahlin
 *
 */
@Deprecated
public abstract class ActorContainer {

    /**
     * Index to x position.
     */
    public final static int X_POS = 0;
    /**
     * Index to y position.
     */
    public final static int Y_POS = 1;
    /**
     * Index to z position.
     */
    public final static int Z_POS = 2;

    public final static int MOVE_VECTOR_X = 3;
    public final static int MOVE_VECTOR_Y = 4;
    public final static int MOVE_VECTOR_Z = 5;
    public final static int FRAME = 6;
    public final static int ROTATION = 7; // z axis rotation angle

    /**
     * All objects can move using a vector
     */
    public Vector2D moveVector = new Vector2D();
    public float[] floatData;
    public final static int MIN_FLOAT_COUNT = 16;

    /**
     * Processes this actor container
     * 
     * @return
     */
    public abstract void process(float deltaTime);

    /**
     * Updates the movement according to the specified acceleration (x and y axis) and time
     * 
     * @param x Acceleration on x axis
     * @param y Acceleration on y axis
     * @param deltaTime Time since last time movement was updated, ie elapsed time.
     */
    public void accelerate(float x, float y, float deltaTime) {
        floatData[MOVE_VECTOR_X] += x * deltaTime;
        floatData[MOVE_VECTOR_Y] += y * deltaTime;
    }

    /**
     * Applies movement and gravity to position
     * 
     * @param deltaTime
     */
    public void move(float deltaTime) {
        floatData[X_POS] += deltaTime * moveVector.vector[VecMath.X] * moveVector.vector[Vector2D.MAGNITUDE] +
                floatData[MOVE_VECTOR_X] * deltaTime;
        floatData[Y_POS] += deltaTime * moveVector.vector[VecMath.Y] * moveVector.vector[Vector2D.MAGNITUDE] +
                floatData[MOVE_VECTOR_Y] * deltaTime;
    }

}
