package com.nucleus.light;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for lightsources, the light is always stored as VEC4.
 * This class, and sublcasses, can be serialized using GSOn
 */
public abstract class Light {

    public static final String COLOR = "color";
    public static final String TYPE = "type";
    public static final String INTENSITY = "intensity";
    public static final String POSITION = "position";

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

    Light(Type type, float[] position, float[] color, float intensity) {
        if (type == null || position == null || color == null) {
            throw new IllegalArgumentException("Null parameter");
        }
        this.type = type;
        this.position = position;
        this.color = color;
        this.intensity = intensity;
    }

    @SerializedName(COLOR)
    private float[] color = new float[] { 1, 1, 1 };
    @SerializedName(TYPE)
    private Type type;
    @SerializedName(INTENSITY)
    private float intensity = 1;
    @SerializedName(POSITION)
    private float[] position;

    /**
     * Returns a reference to light color, any changes done will be reflected here
     * 
     * @return Reference to light color values, 3 values (RGB)
     */
    public float[] getColor() {
        return color;
    }

    /**
     * Returns a reference to light position
     * 
     * @return Light position, 3 values (XYZ)
     */
    public float[] getPosition() {
        return position;
    }

}
