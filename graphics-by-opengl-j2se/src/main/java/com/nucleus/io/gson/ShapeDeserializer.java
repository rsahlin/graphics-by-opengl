package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.vecmath.Shape;

public class ShapeDeserializer extends NucleusDeserializer implements JsonDeserializer<Shape> {

    @Override
    public Shape deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Shape.Type s = Shape.Type.valueOf(obj.get(Shape.TYPE).getAsString());
        Shape shape = (Shape) gson.fromJson(json, s.typeClass);
        postDeserialize(shape);
        return shape;
    }

}
