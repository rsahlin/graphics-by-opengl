package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.bounds.Bounds;
import com.nucleus.io.GSONSceneFactory;
import com.nucleus.scene.AbstractNode.NodeTypes;
import com.nucleus.scene.Node;
import com.nucleus.vecmath.Shape;

/**
 * Implementation of graphics-by-opengl node deserialization for {@link Node} base scenes.
 * This shall return the correct Node implementations for graphics-by-opengl
 * If subclasses register a different deserializer they must make sure to call super.
 * {@link #deserialize(JsonElement, Type, JsonDeserializationContext)}
 * 
 * Avoid using directly use {@link GSONSceneFactory} instead
 * 
 * 
 * @author Richard Sahlin
 *
 */
public class NucleusNodeDeserializer extends NucleusDeserializer<Node> implements JsonDeserializer<Node> {

    protected BoundsDeserializer boundsDeserializer = new BoundsDeserializer();
    protected ShapeDeserializer shapeDeserializer = new ShapeDeserializer();

    public NucleusNodeDeserializer() {
        addNodeTypes(NodeTypes.values());
    }

    @Override
    public void registerTypeAdapter(GsonBuilder builder) {
        builder.registerTypeAdapter(Bounds.class, boundsDeserializer);
        builder.registerTypeAdapter(Node.class, this);
        builder.registerTypeAdapter(Shape.class, shapeDeserializer);
    }

    @Override
    public void setGson(Gson gson) {
        super.setGson(gson);
        boundsDeserializer.setGson(gson);
        shapeDeserializer.setGson(gson);
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
