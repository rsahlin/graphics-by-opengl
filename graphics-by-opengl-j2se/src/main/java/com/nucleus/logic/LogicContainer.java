package com.nucleus.logic;

import com.nucleus.vecmath.Vector2D;

/**
 * For objects containing logic
 * 
 * @author Richard Sahlin
 *
 */
public abstract class LogicContainer {

    /**
     * All objects can move using a vector
     */
    public Vector2D moveVector = new Vector2D();
    public float[] floatData;
    public int[] intData;
    public final static int MIN_FLOAT_COUNT = 16;
    public final static int MIN_INT_COUNT = 8;

    /**
     * Processes this logic container
     * 
     * @return
     */
    public abstract void process(float deltaTime);

}
