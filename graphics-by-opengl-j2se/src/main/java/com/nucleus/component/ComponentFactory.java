package com.nucleus.component;

import com.nucleus.common.TypeResolver;

/**
 * Creates component implementations - used when deserializing from JSON
 * 
 * @author Richard Sahlin
 *
 */
public class ComponentFactory {

    public enum Type {
        component(Component.class);
        private final Class<?> theClass;

        private Type(Class<?> theClass) {
            this.theClass = theClass;
        }

        /**
         * Returns the class to instantiate for the different types
         * 
         * @return
         */
        public Class<?> getTypeClass() {
            return theClass;
        }
    }

    /**
     * Returns a new instance of the component of the specified type
     * 
     * @param type The name of the component to return
     * @return A new instance, or null if invalid name
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Component create(String type) throws InstantiationException, IllegalAccessException {
        try {
            Type t = Type.valueOf(type);
            return (Component) t.theClass.newInstance();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Returns a new instance of the component of the specified type.
     * 
     * @param type
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public Component create(TypeResolver<?> typeClass) throws InstantiationException, IllegalAccessException {
        return (Component) typeClass.getTypeClass().newInstance();
    }

}
