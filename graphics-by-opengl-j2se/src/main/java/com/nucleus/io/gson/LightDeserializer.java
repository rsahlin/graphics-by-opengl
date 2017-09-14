package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.light.Light;
import com.nucleus.scene.NodeType;

/**
 * Implementation for light deserializer, the light class is abstract, read the type field and return correct
 * implementation.
 *
 */
public class LightDeserializer extends NucleusDeserializer implements JsonDeserializer<Light> {

    @Override
    public Light deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        NodeType t = NodeType.valueOf(obj.get(Light.TYPE).getAsString());
        Light light = (Light) gson.fromJson(json, t.getTypeClass());
        postDeserialize(light);
        return light;
    }

}
