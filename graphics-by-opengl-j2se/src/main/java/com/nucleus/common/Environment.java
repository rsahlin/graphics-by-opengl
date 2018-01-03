package com.nucleus.common;

import java.util.HashMap;
import java.util.Map;

import com.nucleus.SimpleLogger;

/**
 * Environment settings
 *
 */
public class Environment {

    public enum Property {
        /**
         * Used for runtime switch of debug checks
         */
        DEBUG("com.nucleus.debug"),
        /**
         * Key for setting if EGL14 surface should be used - hint to subclasses
         * true / false
         */
        EGL14SURFACE("com.nucleus.egl14surface"),
        /**
         * Key for setting number of requested samples - hint to subclasses
         */
        SAMPLES("com.nucleus.samples"),
        /**
         * Key for setting egl sleep, in millis, after swapping buffer - ready by {@link EGLSurfaceView}
         */
        EGLSLEEP("com.nucleus.eglsleep"),
        /**
         * Key for setting egl wait client - true / false
         */
        EGLWAITCLIENT("com.nucleus.eglwaitclient"),
        /**
         * EGL swap interval, only works if using eglsurface
         */
        EGLSWAPINTERVAL("com.nucleus.eglswapinterval");
        

        public final String key;

        private Property(String key) {
            this.key = key;
        }

    }
    
    private Map<Property, String> properties = new HashMap();
    

    private static Environment environment;

    private Environment() {
        loadProperties();
    }

    public static Environment getInstance() {
        if (environment == null) {
            environment = new Environment();
        }
        return environment;
    }

    /**
     * Checks each {@link Property} and stores value in local hashmap , call {@link #getProperty(Property)} to read value.
     */
    public void loadProperties() {
        for (Property p : Property.values()) {
            String value = getProperty(p);
            if (value != null) {
                properties.put(p, value);
                SimpleLogger.d(getClass(), "Read environment property " + p + " to " + value);
            }
        }
        
    }
    
    /**
     * Sets the property value to the local map and to system properties. The value will be retained when
     * calling {@link #loadProperties()}
     * 
     * @param property
     * @param value
     */
    public void setProperty(Property property, String value) {
        properties.put(property, value);
        System.setProperty(property.key, value);
        SimpleLogger.d(getClass(), "Set environment property " + property + " to " + value);
    }
    
    
    /**
     * Returns the system property for the key defined in the Property enum.
     * This will fetch the property from System.getProperty()
     * @param property
     * @return The property value, or null if not set.
     */
    public String getProperty(Property property) {
        return System.getProperty(property.key);
    }
    
    /**
     * Returns true if the system property is defined as 'true', false if property set but not 'true'.
     * If no property is set then defaultValue is returned.
     * This will lookup the property in the local hashmap storage.
     * To trigger refresh of properties call {@link #loadProperties()}
     * 
     * @param property
     * @param defaultValue Value returned if property not set, or empty
     * @return
     */
    public boolean isProperty(Property property, boolean defaultValue) {
        String p = properties.get(property);
        return p != null && p.length() > 0 ? p.equalsIgnoreCase(Constants.TRUE) : defaultValue;
    }

}
