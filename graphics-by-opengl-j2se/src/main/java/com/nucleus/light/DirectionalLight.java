package com.nucleus.light;

/**
 * Directional light - all rays are considered to be paralell from the lightsource.
 *
 */
public class DirectionalLight extends Light {

    DirectionalLight(float[] light, float[] position) {
        super(Light.Type.DIRECITONAL, light, position);
    }

}
