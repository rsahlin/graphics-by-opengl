package com.nucleus.light;

/**
 * Singleton class to handle global illumination
 * 
 */
public class GlobalLight {

    private float[] ambient = new float[] { 1f, 1f, 1f, 1 };

    /**
     * Position of light
     */
    private float[] lightPosition = new float[3];

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

    public void getLightMatrix(float[] matrix) {
    }

}
