package com.nucleus.properties;

import java.util.HashMap;

/**
 * Takes care of parsing and passing on key/value properties.
 * The manager does not have any connection to the handlers as such, they must be registered
 * 
 * @author Richard Sahlin
 *
 */
public class PropertyManager {

    public final static String TRUE = "true";
    public final static String FALSE = "false";

    private static PropertyManager propertyManager = new PropertyManager();

    private HashMap<String, PropertyHandler> handlers = new HashMap<>();

    private HashMap<String, String> properties = new HashMap<>();

    /**
     * Interface for handling of a specific key
     * 
     * @author Richard Sahlin
     *
     */
    public interface PropertyHandler {
        /**
         * Called when {@linkplain PropertyManager#setProperty(String, String)} is called with a registered key.
         * 
         * @param key The key
         * @param value The value
         * @return True to set the property in the property manager, if false is returned the property key/value is
         * not set in the {@linkplain PropertyManager}
         */
        public boolean handleProperty(String key, String value);

        /**
         * Called when {@linkplain PropertyManager#setObjectProperty(Object, String, String)} is called with a
         * registered key.
         * This method is normally used to register the object with the receiver
         * 
         * @param obj The object related to the key/value
         * @param key
         * @param value
         * @return
         */
        public boolean handleObjectProperty(Object obj, String key, String value);

        /**
         * Returns the key that the handler can understand.
         * 
         * @return
         */
        public String getHandlerKey();

    }

    /**
     * Returns an instance of this class, this method will always return the same instance.
     * 
     * @return The same instance of this class
     */
    public static PropertyManager getInstance() {
        return propertyManager;
    }

    /**
     * Registers a key to be handled by the specified handler, if a handler was already registered for the
     * specified key it is replaced.
     * After this method any call to {@linkplain PropertyManager#setProperty(String, String)} with the specified key
     * will invoke the {@linkplain PropertyHandler#handleProperty(String, String)} method.
     * 
     * @param key The key to register a handler for, or null to register with the handlers key
     * @param handler Handler for the key, or null to remove.
     */
    public void registerKey(String key, PropertyHandler handler) {
        if (key == null) {
            key = handler.getHandlerKey();
        }
        handlers.put(key, handler);
    }

    /**
     * Sets the property to this propertymanager, if a registered handler exists for the key it is called.
     * If a handler is registered for the key it is called and if true is returned the key/value is set as property in
     * this class.
     * If a handler is not registered the key/value is set in this class
     * 
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        PropertyHandler handler = handlers.get(key);
        if (handler != null) {
            if (handler.handleProperty(key, value)) {
                properties.put(key, value);
            }
        } else {
            properties.put(key, value);
        }
    }

    /**
     * Sets the property for the specified object, if {@linkplain PropertyHandler} is registered for the key
     * the {@linkplain PropertyHandler#handleObjectProperty(Object, String, String)} method is called with the object.
     * 
     * @param obj
     * @param key
     * @param value
     */
    public void setObjectProperty(Object obj, String key, String value) {
        PropertyHandler handler = handlers.get(key);
        if (handler != null) {
            if (handler.handleObjectProperty(obj, key, value)) {
                properties.put(key, value);
            }
        } else {
            properties.put(key, value);
        }

    }

    /**
     * Returns the value for the specified key, if set, otherwise null
     * 
     * @param key
     * @return The value for the key, or null if not set.
     */
    public String getProperty(String key) {
        return properties.get(key);
    }
    
}
