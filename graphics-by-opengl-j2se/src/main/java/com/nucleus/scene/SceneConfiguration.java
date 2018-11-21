package com.nucleus.scene;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.nucleus.io.gson.AbstractNucleusDeserializerImpl;

/**
 * Root configuration for the scene
 *
 * @param <T>
 */
public class SceneConfiguration<T> extends AbstractNucleusDeserializerImpl<Node>
        implements JsonDeserializer<SceneConfiguration<?>> {

    public final static String DATA = "DATA";

    @SerializedName(DATA)
    private T data;

    public T getData() {
        return data;
    }

    @Override
    public SceneConfiguration<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        SceneConfiguration<?> config = gson.fromJson(json, type);
        postDeserialize(config);
        return config;
    }

    @Override
    public void registerTypeAdapter(GsonBuilder builder) {
        // No need to register any adapter
    }

}
