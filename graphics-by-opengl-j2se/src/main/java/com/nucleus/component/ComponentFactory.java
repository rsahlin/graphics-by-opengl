package com.nucleus.component;

import com.nucleus.common.Type;

/**
 * Creates component implementations - used when deserializing from JSON
 * Singleton class that is fetched using {@link #getInstance()}
 * 
 * @author Richard Sahlin
 *
 */
public class ComponentFactory {

    private static ComponentFactory factory;

    /**
     * Returns the singleton instance of the factory
     * 
     * @return
     */
    public static ComponentFactory getInstance() {
        if (factory == null) {
            factory = new ComponentFactory();
        }
        return factory;
    }

    /**
     * Returns a new instance of the component of the specified type.
     * 
     * @param typeClass
     * @return A new instance of the component.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public Component create(Type<?> typeClass) throws InstantiationException, IllegalAccessException {
        return (Component) typeClass.getTypeClass().newInstance();
    }

}
