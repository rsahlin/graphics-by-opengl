package com.nucleus.io.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.common.TypeResolver;

/**
 * Base class for a {@link NucleusDeserializer}
 * Use this to get basic support for gson and to register type adapters.
 *
 * @param <T> The Node class that shall be serialized
 */
public abstract class NucleusDeserializerImpl<T> implements NucleusDeserializer<T> {

    public final static String NODETYPE_JSON_KEY = "type";

    protected TypeResolver nodeResolver = TypeResolver.getInstance();

    /**
     * The gson instance to use when deserializing
     */
    protected Gson gson;

    /**
     * Set the gson instance to be used, this is called after {@link #registerTypeAdapter(GsonBuilder)}
     * Subclasses shall call super{@link #setGson(Gson)}
     * 
     * @param gson
     */
    @Override
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    /**
     * Returns the gson instance to be used with this deserializer.
     * Must be set by calling {@link #setGson(Gson)}
     * 
     * @return
     */
    @Override
    public Gson getGson() {
        return gson;
    }

    /**
     * Checks if implements post deserialize, then call {@linkplain PostDeserializable#postDeserialize()}
     * 
     * @param deserialized The class that has been deserialized
     */
    @Override
    public void postDeserialize(Object deserialized) {
        if (deserialized instanceof PostDeserializable) {
            ((PostDeserializable) deserialized).postDeserialize();
        }
    }

    /**
     * Adds a list with known type name/classes to the deserializer.
     * Use this to add custom nodes for import.
     * 
     * @param types
     */
    @Override
    public void addNodeTypes(com.nucleus.common.Type<T>[] types) {
        nodeResolver.registerTypes(types);
    }

    /**
     * Adds a type name/class to the deserializer.
     * Use this to add custom nodes for import.
     * 
     * @param types
     */
    @Override
    public void addNodeType(com.nucleus.common.Type<T> type) {
        nodeResolver.registerType(type);
    }

    /**
     * Register the type adapter(s) needed when serializing JSON
     * 
     * @param builder The gson builder used to serialize JSON content
     */
    @Override
    public abstract void registerTypeAdapter(GsonBuilder builder);

}
