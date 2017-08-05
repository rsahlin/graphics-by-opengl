package com.nucleus.common;

import java.util.Hashtable;
import java.util.List;

/**
 * Creates the classes from type names, this is for instance used when serializing as a means to use specialized type
 * names instead of implementing classes.
 * As the implementing classes may change, leaving already serialized classnames unusable, use this method to decouple
 * serialized type names from implementing classes.
 * 
 * @author Richard Sahlin
 *
 */
public class TypeResolver {
    private static final String NO_TYPE_FOR_NAME = "No type for:";
    private static TypeResolver factory;
    /**
     * Holds registered Strings, can be used to fetch type from string name
     */
    private Hashtable<String, Type<?>> types = new Hashtable<>();

    /**
     * Returns the singleton instance of the factory.
     * 
     * @return
     */
    public static TypeResolver getInstance() {
        if (factory == null) {
            factory = new TypeResolver();
        }
        return factory;
    }

    /**
     * Adds a resolver for the specified type, if there already is a resolver for the specified type an exception is
     * thrown.
     * 
     * @param type
     * @throws IllegalArgumentException If a type already has been registered with the same type name.
     */
    public void registerType(Type<?> type) {
        if (types.put(type.getName(), type) != null) {
            throw new IllegalArgumentException("Already added resolver for " + type.getName());
        }
    }

    /**
     * Registers a list of types
     * 
     * @param types
     * @throws IllegalArgumentException If a type already has been registered with the same name as is in the list
     */
    public void registerTypes(List<Type<?>> types) {
        for (Type<?> t : types) {
            registerType(t);
        }
    }

    /**
     * Returns the class for the type name
     * 
     * @param name Name of type, as registered by calling {@link #registerType(Type)}
     * @return
     * @throws IllegalArgumentException If there is no type registered for the name
     */
    public Class<?> getTypeClass(String name) {
        Type<?> type = types.get(name);
        if (type == null) {
            throw new IllegalArgumentException(NO_TYPE_FOR_NAME + name);
        }
        return type.getTypeClass();
    }

    /**
     * Returns a new instance of the component of the specified type
     * 
     * @param name The name of the object to return
     * @return A new instance
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException If there is no type registered for the name
     */
    public Object create(String name) throws InstantiationException, IllegalAccessException {
        Type<?> type = types.get(name);
        if (type == null) {
            throw new IllegalArgumentException(NO_TYPE_FOR_NAME + name);
        }
        return create(type);
    }

    /**
     * Returns the object for the specified type
     * 
     * @param type
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object create(Type<?> type) throws InstantiationException, IllegalAccessException {
        return type.getTypeClass().newInstance();
    }

    /**
     * Clears all registered types
     */
    public void clear() {
        types.clear();
    }

}
