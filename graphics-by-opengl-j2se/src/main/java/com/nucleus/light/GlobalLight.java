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
    private float[] lightPosition = new float[] { 0, 5000, 10000 };

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
     * Deprecated - this is not a property of a global light, but rather a node property.
     * Returns the global ambient light
     * 
     * @return
     */
    @Deprecated
    public float[] getAmbient() {
        return ambient;
    }

    /**
     * Reads the global light position and stores in dest array at offset
     * 
     * @param dest
     * @param offset
     */
    public void getLightPosition(float[] dest, int offset) {
        dest[offset++] = lightPosition[0];
        dest[offset++] = lightPosition[1];
        dest[offset++] = lightPosition[2];
    }

    /**
     * Returns a reference to the light position
     * 
     * @return
     */
    public float[] getLightPosition() {
        return lightPosition;
    }

    public void getLightMatrix(float[] matrix) {
    }

}
