package com.nucleus.properties;

/**
 * Helper class for handling a property containing of key and value
 * 
 * @author Richard Sahlin
 *
 */
public class Property {

    private String key;
    private String value;

    public final static char DELIMITER = ':';

    public Property(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Creates a new property from the string value, the key/value will be taken from the property by splitting it
     * at the first ':' char.
     * 
     * @param property
     * @return The property containing key/value from the string
     */
    public static Property create(String property) {
        int split = property.indexOf(':');
        if (split == -1) {
            return null;
        }
        Property p = new Property(property.substring(0, split), property.substring(split + 1));
        return p;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    /**
     * 
     * Split the value into 2 strings if the {@value #DELIMITER} character is present.
     * 
     * @param value
     * @return Two strings if value contains {@value #DELIMITER}, otherwise one string
     */
    public static String[] split(String value) {
        int index = value.indexOf(':');
        if (index >= 0) {
            return new String[] { value.substring(0, index), value.substring(index + 1) };
        } else {
            return new String[] { value };
        }
    }

}
