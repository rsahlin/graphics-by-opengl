package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nucleus.vecmath.Shape;

/**
 * Helper class for reading Shape subclasses from gson.
 * TODO Perhaps move this class to vecmath?
 *
 */
public class ShapeDeserializer extends NucleusDeserializer implements JsonDeserializer<Shape> {

    @Override
    public Shape deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        /**
         * TODO Perhaps use TypeResolver instead of Shape enum so that any class extending Shape can be used
         * as long as it is registered?
         * This means that the shape type must be an instance of Type and this interface is not, and should not be, in
         * Vecmath.
         */
        Shape.Type s = Shape.Type.valueOf(obj.get(Shape.TYPE).getAsString());
        Shape shape = (Shape) gson.fromJson(json, s.typeClass);
        postDeserialize(shape);
        return shape;
    }

}
