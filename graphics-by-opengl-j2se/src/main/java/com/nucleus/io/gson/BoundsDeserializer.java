package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.bounds.Bounds;

public class BoundsDeserializer implements JsonDeserializer<Bounds> {

    @Override
    public Bounds deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        com.nucleus.bounds.Bounds.Type t = com.nucleus.bounds.Bounds.Type
                .valueOf(obj.get(Bounds.SerializeNames.type.name()).getAsString());
        float[] values = null;
        if (obj.get(Bounds.SerializeNames.bounds.name()) != null) {
            JsonArray array = obj.get(Bounds.SerializeNames.bounds.name()).getAsJsonArray();
            values = new float[array.size()];
            for (int i = 0; i < array.size(); i++) {
                values[i] = array.get(i).getAsFloat();
            }
        }
        return Bounds.create(t, values);
    }

}
