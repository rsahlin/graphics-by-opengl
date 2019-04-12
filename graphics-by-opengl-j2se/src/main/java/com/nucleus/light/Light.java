package com.nucleus.light;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for lightsources, the light is always stored as VEC4.
 * This class, and subclasses, can be serialized using GSOn
 */
public abstract class Light {

    public static final int COLOR_INDEX = 0;
    public static final int INTENSITY_INDEX = 3;
    public static final int POSITION_INDEX = 4;
    public static final int SKYCOLOR_INDEX = 8;
    // Must be multiple of 4
    public static final int DATASIZE = 12;

    /**
     * Light color, intensity and position, 7 values
     */
    public static final String LIGHT = "light";
    public static final String TYPE = "type";

    /**
     * The different light types, this specifies how light is distributed
     */
    public enum Type implements com.nucleus.common.Type<Light> {
        AMBIENT(Ambient.class),
        DIRECITONAL(null),
        POINT(null),
        SPOT(null);

        private final Class<?> theClass;

        private Type(Class<?> theClass) {
            this.theClass = theClass;
        }

        /**
         * Returns the class to instantiate for the different types
         * 
         * @return
         */
        @Override
        public Class<?> getTypeClass() {
            return theClass;
        }

        @Override
        public String getName() {
            return toString();
        }

    }

    Light(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Light type is null");
        }
        this.type = type;
    }

    /**
     * Creates a new light
     * 
     * @param type
     * @param light RGB, intensity, XYZ
     */
    Light(Type type, float[] values) {
        if (type == null || values == null || values.length != DATASIZE) {
            throw new IllegalArgumentException("Null parameter or wrong length of light arrays");
        }
        this.type = type;
        System.arraycopy(values, 0, this.light, 0, light.length);

    }

    @SerializedName(LIGHT)
    private float[] light = new float[DATASIZE];
    @SerializedName(TYPE)
    private Type type;

    /**
     * Returns a reference to light colo, intensity and position - any changes done will be reflected here
     * 
     * @return Reference to light color, intensity and position values
     */
    public float[] getLight() {
        return light;
    }

    /**
     * Sets the intensity of the light
     * 
     * @param intensity
     */
    public void setIntensity(float intensity) {
        light[INTENSITY_INDEX] = intensity;
    }

}
