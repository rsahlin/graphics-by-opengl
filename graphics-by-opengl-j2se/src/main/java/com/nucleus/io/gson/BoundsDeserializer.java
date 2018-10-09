package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.bounds.Bounds;
import com.nucleus.scene.Node;

public class BoundsDeserializer extends NucleusDeserializer<Node> implements JsonDeserializer<Bounds> {

    @Override
    public Bounds deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        com.nucleus.bounds.Bounds.Type t = Bounds.Type.valueOf(obj.get(Bounds.TYPE).getAsString());
        Bounds bounds = (Bounds) gson.fromJson(json, t.getTypeClass());
        postDeserialize(bounds);
        return bounds;
    }

    @Override
    public void registerTypeAdapter(GsonBuilder builder) {
        // No need to register any adapter
    }

}
