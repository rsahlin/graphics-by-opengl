package com.nucleus.io;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.Error;
import com.nucleus.scene.ViewNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeType;
import com.nucleus.scene.SwitchNode;

/**
 * Implementation of graphics-by-opengl node deserialization, this shall return the correct Node implementations
 * for graphics-by-opengl
 * If subclasses register a different deserializer they must make sure to call super.
 * {@link #deserialize(JsonElement, Type, JsonDeserializationContext)}
 * 
 * @author Richard Sahlin
 *
 */
public class NucleusNodeDeserializer implements JsonDeserializer<Node> {

    /**
     * The gson instance to use when deserializing
     */
    protected Gson gson;
    
    /**
     * Sets the gson instance to be used when deserializing
     * 
     * @param gson
     */
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Node deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        NodeType t = NodeType.valueOf(obj.get("type").getAsString());
        switch (t) {
        case node:
            return gson.fromJson(json, Node.class);
        case viewnode:
            return gson.fromJson(json, ViewNode.class);
        case switchnode:
            return gson.fromJson(json, SwitchNode.class);
        default:
            throw new IllegalArgumentException(Error.NOT_IMPLEMENTED.message);
        }
    }

}
