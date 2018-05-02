package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.common.TypeResolver;
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

    private TypeResolver nodeResolver = TypeResolver.getInstance();

    public NucleusNodeDeserializer() {
        addNodeTypes(NodeTypes.values());
    }

    /**
     * Adds a list with known type name/classes to the deserializer.
     * Use this to add custom nodes for import.
     * 
     * @param types
     */
    public void addNodeTypes(com.nucleus.common.Type<Node>[] types) {
        nodeResolver.registerTypes(types);
    }

    /**
     * Adds a type name/class to the deserializer.
     * Use this to add custom nodes for import.
     * 
     * @param types
     */
    public void addNodeType(com.nucleus.common.Type<Node> type) {
        nodeResolver.registerType(type);
    }

    @Override
    public Node deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        JsonElement element = obj.get(NODETYPE_JSON_KEY);
        if (element == null) {
            throw new IllegalArgumentException("Node does not contain:" + NODETYPE_JSON_KEY);
        }
        Node node = null;
        com.nucleus.common.Type<?> t = nodeResolver.getType(element.getAsString());
        node = (Node) gson.fromJson(json, t.getTypeClass());
        postDeserialize(node);
        return node;

    }
}
