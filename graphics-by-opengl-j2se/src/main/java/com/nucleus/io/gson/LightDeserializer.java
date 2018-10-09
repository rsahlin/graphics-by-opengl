package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.light.Light;
import com.nucleus.scene.AbstractNode.NodeTypes;
import com.nucleus.scene.Node;

/**
 * Implementation for light deserializer, the light class is abstract, read the type field and return correct
 * implementation.
 *
 */
public class LightDeserializer extends NucleusDeserializer<Node> implements JsonDeserializer<Light> {

    @Override
    public Light deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        NodeTypes t = NodeTypes.valueOf(obj.get(Light.TYPE).getAsString());
        Light light = (Light) gson.fromJson(json, t.getTypeClass());
        postDeserialize(light);
        return light;
    }

    @Override
    public void registerTypeAdapter(GsonBuilder builder) {
        // No need to register any adapter
    }

}
