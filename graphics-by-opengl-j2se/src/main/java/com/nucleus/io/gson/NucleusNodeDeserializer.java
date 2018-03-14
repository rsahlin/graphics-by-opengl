package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.NodeTypes;

/**
 * Implementation of graphics-by-opengl node deserialization, this shall return the correct Node implementations
 * for graphics-by-opengl
 * If subclasses register a different deserializer they must make sure to call super.
 * {@link #deserialize(JsonElement, Type, JsonDeserializationContext)}
 * 
 * @author Richard Sahlin
 *
 */
public class NucleusNodeDeserializer extends NucleusDeserializer implements JsonDeserializer<Node> {

    // TODO where is a good place to store this constant?
    public final static String NODETYPE_JSON_KEY = "type";

    @Override
    public Node deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        JsonElement element = obj.get(NODETYPE_JSON_KEY);
        if (element == null) {
            throw new IllegalArgumentException("Node does not contain:" + NODETYPE_JSON_KEY);
        }
        try {
            Node node = null;
            NodeTypes t = NodeTypes.valueOf(element.getAsString());
            switch (t) {
                case node:
                    // Throw runtimeexception since IllegalArgumentException is used to catch invalid node type.
                    throw new RuntimeException("Can not deserialize vanilla Node - use one of the subclasses");
                default:
                    node = (Node) gson.fromJson(json, t.getTypeClass());
                    break;
            }
            postDeserialize(node);
            return node;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown node type " + element.getAsString());
        }

    }
}
