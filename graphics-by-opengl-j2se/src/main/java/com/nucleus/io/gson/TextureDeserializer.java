package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureType;

public class TextureDeserializer implements JsonDeserializer<Texture2D> {
    // TODO where is a good place to store this constant?
    public final static String TEXTURE_JSON_KEY = "textureType";

    @Override
    public Texture2D deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        JsonElement jsonType = obj.get(TEXTURE_JSON_KEY);
        if (jsonType == null) {
            throw new IllegalArgumentException("Texture must define " + TEXTURE_JSON_KEY);
        }
        TextureType texType = TextureType.valueOf(jsonType.getAsString());
        return (Texture2D) new Gson().fromJson(json, texType.getImplementation());
    }
}
