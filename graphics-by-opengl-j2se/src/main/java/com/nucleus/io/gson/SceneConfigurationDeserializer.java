package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.nucleus.scene.Node;
import com.nucleus.scene.SceneConfiguration;

public class SceneConfigurationDeserializer extends NucleusDeserializer<Node>
        implements JsonDeserializer<SceneConfiguration<?>> {

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
