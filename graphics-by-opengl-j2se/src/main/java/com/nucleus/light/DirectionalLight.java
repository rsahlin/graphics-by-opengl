package com.nucleus.light;

/**
 * Directional light - all rays are considered to be paralell from the lightsource.
 *
 */
public class DirectionalLight extends Light {

    DirectionalLight(float[] position, float[] color, int intensity) {
        super(Light.Type.DIRECITONAL, position, color, intensity);
    }

}
