package com.nucleus.light;

/**
 * Singleton class to handle global illumination
 * 
 */
public class GlobalLight {

    private float[] ambient = new float[] { 0.5f, 0.5f, 0.5f, 1 };

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

}
