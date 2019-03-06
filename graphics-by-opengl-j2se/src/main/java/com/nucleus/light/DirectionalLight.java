package com.nucleus.light;

/**
 * Directional light - all rays are considered to be paralell from the lightsource.
 *
 */
public class DirectionalLight extends Light {

    DirectionalLight(float[] values) {
        super(Light.Type.DIRECITONAL, values);
    }

}
