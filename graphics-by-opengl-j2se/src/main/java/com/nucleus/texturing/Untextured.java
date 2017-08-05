package com.nucleus.texturing;

import static com.nucleus.vecmath.Rectangle.HEIGHT;
import static com.nucleus.vecmath.Rectangle.WIDTH;
import static com.nucleus.vecmath.Rectangle.X;
import static com.nucleus.vecmath.Rectangle.Y;

import com.google.gson.annotations.SerializedName;
import com.nucleus.vecmath.Rectangle;

/**
 * For untextured objects, can define a shading which corresponds to the untextured program to use.
 * 
 * @author Richard Sahlin
 *
 */
public class Untextured extends Texture2D {
    
    @SerializedName("shading")
    private Shading shading;
    
    protected Untextured() {
        super();
    }

    protected Untextured(Untextured source) {
        set(source);
    }

    /**
     * Copies the data from the source into this
     * 
     * @param source
     */
    protected void set(Untextured source) {
        super.set(source);
        this.shading = source.shading;
    }

    /**
     * Returns the (fragment) shader to use for the untextured object
     * 
     * @return
     */
    public Shading getShading() {
        return shading;
    }

    @Override
    public float[] createQuadArray(Rectangle rectangle, int vertexStride, float z) {
        if (shading == Shading.parametric) {
            return createQuadPositionsUVIndexed(rectangle, vertexStride, 0,
                    new float[] { -1, 1, 1, 1, 1, -1, -1, -1 });
        }
        return super.createQuadArray(rectangle, vertexStride, z);
    }

    public float[] createQuadPositionsUVIndexed(Rectangle rectangle, int vertexStride, float z, float[] UV) {
        float[] values = rectangle.getValues();
        // TODO How to handle Y axis going other direction?
        float[] quadPositions = new float[vertexStride * 4];
        com.nucleus.geometry.MeshBuilder.setPositionUV(values[X], values[Y],
                z, UV[0], UV[1], quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPositionUV(values[X] + values[WIDTH], values[Y], z, UV[2], UV[3],
                quadPositions, vertexStride);
        com.nucleus.geometry.MeshBuilder.setPositionUV(values[X] + values[WIDTH], values[Y] - values[HEIGHT],
                z, UV[4], UV[5], quadPositions, vertexStride * 2);
        com.nucleus.geometry.MeshBuilder.setPositionUV(values[X], values[Y] - values[HEIGHT], z, UV[6], UV[7],
                quadPositions, vertexStride * 3);
        return quadPositions;
    }

}
