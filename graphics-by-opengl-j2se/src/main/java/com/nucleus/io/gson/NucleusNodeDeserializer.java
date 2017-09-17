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

    @Override
    public Node deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        NodeTypes t = NodeTypes.valueOf(obj.get(Node.TYPE).getAsString());
        Node node = (Node) gson.fromJson(json, t.getTypeClass());
        postDeserialize(node);
        return node;
    }

}
