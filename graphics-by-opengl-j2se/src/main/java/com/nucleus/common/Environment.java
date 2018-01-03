package com.nucleus.common;

/**
 * Environment settings
 *
 */
public class Environment {

    public enum Property {
        /**
         * Used for runtime switch of debug checks
         */
        DEBUG("com.nucleus.debug");

        public final String key;

        private Property(String key) {
            this.key = key;
        }

    }

    private static Environment environment;

    private Environment() {
    }

    public static Environment getInstance() {
        if (environment == null) {
            environment = new Environment();
        }
        return environment;
    }

    /**
     * Returns true if the system property is defined as 'true', false if property not set or not defined as 'true'
     * 
     * @param property
     * @return
     */
    public boolean isProperty(Property property) {
        String p = System.getProperty(property.key);
        return p != null ? p.equalsIgnoreCase(Constants.TRUE) : false;
    }

}
