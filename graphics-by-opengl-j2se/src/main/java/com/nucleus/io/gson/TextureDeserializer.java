package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.assets.AssetManager;
import com.nucleus.scene.Node;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureType;

/**
 * Serializer for texture objects read from JSON
 * DO NOT USE THIS CLASS DIRECTLY - go through {@link AssetManager} to handle textures
 *
 */
public class TextureDeserializer extends NucleusDeserializer<Node> implements JsonDeserializer<Texture2D> {

    @Override
    public Texture2D deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        JsonElement jsonType = obj.get(Texture2D.TEXTURETYPE);
        if (jsonType == null) {
            throw new IllegalArgumentException("Texture must define " + Texture2D.TEXTURETYPE);
        }
        TextureType texType = TextureType.valueOf(jsonType.getAsString());
        Texture2D texture = (Texture2D) new Gson().fromJson(json, texType.getImplementation());
        postDeserialize(texture);
        return texture;
    }

    @Override
    public void registerTypeAdapter(GsonBuilder builder) {
        // No need to register any adapter
    }

}
