package com.nucleus.light;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for lightsources, the light is always stored as VEC4.
 * This class, and sublcasses, can be serialized using GSOn
 */
public abstract class Light {

    public static final String COLOR = "color";
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
    }

    @SerializedName(COLOR)
    private float[] color = new float[4];
    @SerializedName(TYPE)
    private Type type;

    /**
     * Returns a reference to light color, any changes done will be reflected here
     * 
     * @return Reference to light color values.
     */
    public float[] getColor() {
        return color;
    }

}
