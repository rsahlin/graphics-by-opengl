package com.nucleus.actor;

import com.nucleus.vecmath.Vector2D;

/**
 * For objects containing actor logic
 * 
 * @author Richard Sahlin
 *
 */
public abstract class ActorContainer {

    /**
     * All objects can move using a vector
     */
    public Vector2D moveVector = new Vector2D();
    public float[] floatData;
    public int[] intData;
    public final static int MIN_FLOAT_COUNT = 16;
    public final static int MIN_INT_COUNT = 8;

    /**
     * Processes this actor container
     * 
     * @return
     */
    public abstract void process(float deltaTime);

}
