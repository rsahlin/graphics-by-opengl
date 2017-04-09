package com.nucleus.io.gson;

/**
 * Base implementation of a deserializer
 */
import com.google.gson.Gson;

/**
 * Base class for nodetree deserializer, use this when deserialization will be recursive.
 * 
 * @author Richard Sahlin
 *
 */
public class NucleusDeserializer {

    /**
     * The gson instance to use when deserializing
     */
    protected Gson gson;

    /**
     * Sets the gson instance to be used when deserializing
     * Remember to first set type adapters to the Gson builder, then call GsonBuilder.create() to create
     * a gson instance using the specified type adapters
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

}
