package com.nucleus.properties;

import java.util.HashMap;

/**
 * Takes care of parsing and passing on events, events are short lived, have category and value.
 * The manager does not have any connection to the event handlers as such, they must be registered
 * 
 * @author Richard Sahlin
 *
 */
public class EventManager {

    public final static String TRUE = "true";
    public final static String FALSE = "false";

    private static EventManager eventManager = new EventManager();

    private HashMap<String, EventHandler> handlers = new HashMap<>();

    /**
     * Interface for handling of a specific event category
     * 
     * @author Richard Sahlin
     *
     */
    public interface EventHandler {

        public enum Type {
            /**
             * The event is pointer input
             */
            POINTERINPUT()
        }

        /**
         * Called when {@linkplain EventManager#sendEvent(String, String)} is called with a registered key.
         * 
         * @param category The category
         * @param value The value
         */
        public void handleEvent(String category, String value);

        /**
         * Called when {@linkplain EventManager#sendObjectEvent(Object, String, String)} is called with a
         * registered key.
         * This method is normally used to register the object with the receiver
         * 
         * @param obj The object related to the category/value
         * @param category
         * @param value
         */
        public void handleObjectEvent(Object obj, String category, String value);

        /**
         * Returns the category that the handler can understand.
         * 
         * @return The category the handler can handle
         */
        public String getHandlerCategory();

    }

    /**
     * Returns an instance of this class, this method will always return the same instance.
     * 
     * @return The same instance of this class
     */
    public static EventManager getInstance() {
        return eventManager;
    }

    /**
     * Registers a category to be handled by the specified handler, if a handler was already registered for the
     * specified category it is replaced.
     * After this method any call to {@linkplain EventManager#sendEvent(String, String)} with the specified key
     * will invoke the {@linkplain EventHandler#handleEvent(String, String)} method.
     * 
     * @param category The category to register a handler for, or null to register with the handlers key
     * @param handler Handler for the category
     */
    public void registerCategory(String category, EventHandler handler) {
        if (category == null) {
            category = handler.getHandlerCategory();
        }
        handlers.put(category, handler);
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
    public void sendEvent(String category, String value) {
        EventHandler handler = handlers.get(category);
        if (handler != null) {
            handler.handleEvent(category, value);
        }
    }

    /**
     * Sets the property for the specified object, if {@linkplain EventHandler} is registered for the key
     * the {@linkplain EventHandler#handleObjectEvent(Object, String, String)} method is called with the object.
     * 
     * @param obj
     * @param key
     * @param value
     */
    public void sendObjectEvent(Object obj, String category, String value) {
        EventHandler handler = handlers.get(category);
        if (handler != null) {
            Property p = Property.create(value);
            handler.handleObjectEvent(obj, p.getKey(), p.getValue());
        }

    }
    
}
