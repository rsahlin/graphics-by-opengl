package com.nucleus.io.gson;

/**
 * Base implementation of a deserializer
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Interface for GSON nodetree deserializer, use this when deserialization will be recursive.
 * This shall resolve node classes correctly by registering the correct type adapters.
 * 
 * @author Richard Sahlin
 *
 */
public interface NucleusDeserializer<T> {

    public final static String NODETYPE_JSON_KEY = "type";

    /**
     * Set the gson instance to be used, this is called after {@link #registerTypeAdapter(GsonBuilder)}
     * Subclasses shall call super{@link #setGson(Gson)}
     * 
     * @param gson
     */
    public void setGson(Gson gson);

    /**
     * Returns the gson instance to be used with this deserializer.
     * Must be set by calling {@link #setGson(Gson)}
     * 
     * @return
     */
    public Gson getGson();

    /**
     * Checks if implements post deserialize, then call {@linkplain PostDeserializable#postDeserialize()}
     * 
     * @param deserialized The class that has been deserialized
     */
    public void postDeserialize(Object deserialized);

    /**
     * Adds a list with known type name/classes to the deserializer.
     * Use this to add custom nodes for import.
     * 
     * @param types
     */
    public void addNodeTypes(com.nucleus.common.Type<T>[] types);

    /**
     * Adds a type name/class to the deserializer.
     * Use this to add custom nodes for import.
     * 
     * @param types
     */
    public void addNodeType(com.nucleus.common.Type<T> type);

    /**
     * Register the type adapter(s) needed when serializing JSON
     * 
     * @param builder The gson builder used to serialize JSON content
     */
    public void registerTypeAdapter(GsonBuilder builder);

}
