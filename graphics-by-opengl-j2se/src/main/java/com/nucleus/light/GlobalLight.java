package com.nucleus.light;

/**
 * Singleton class to handle global illumination
 * 
 */
public class GlobalLight {

    private float[] ambient = new float[] { 1f, 1f, 1f, 1 };
    
    /**
     * Direction of global light vector.
     */
    private float[] lightVector = new float[] { 40f, 0, 0 };

    private static GlobalLight globalLight = new GlobalLight();

    /**
     * Returns the singleton instance of the global light
     * 
     * @return
     */
    public static GlobalLight getInstance() {
        return globalLight;
    }

    /**
     * Returns the global ambient light
     * 
     * @return
     */
    public float[] getAmbient() {
        return ambient;
    }
    
    /**
     * Returns the direction of the global light
     * @return
     */
    public float[] getLightVector() {
        return lightVector;
    }
    

}
