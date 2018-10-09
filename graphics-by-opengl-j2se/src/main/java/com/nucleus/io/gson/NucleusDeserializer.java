package com.nucleus.io.gson;

/**
 * Base implementation of a deserializer
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.common.TypeResolver;

/**
 * Base class for GSON nodetree deserializer, use this when deserialization will be recursive.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class NucleusDeserializer<T> {

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
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    /**
     * Checks if implements post deserialize, then call {@linkplain PostDeserializable#postDeserialize()}
     * 
     * @param deserialized The class that has been deserialized
     */
    protected void postDeserialize(Object deserialized) {
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
    public void addNodeTypes(com.nucleus.common.Type<T>[] types) {
        nodeResolver.registerTypes(types);
    }

    /**
     * Adds a type name/class to the deserializer.
     * Use this to add custom nodes for import.
     * 
     * @param types
     */
    public void addNodeType(com.nucleus.common.Type<T> type) {
        nodeResolver.registerType(type);
    }

    /**
     * Register the type adapter(s) needed when serializing JSON
     * 
     * @param builder The gson builder used to serialize JSON content
     */
    public abstract void registerTypeAdapter(GsonBuilder builder);

}
