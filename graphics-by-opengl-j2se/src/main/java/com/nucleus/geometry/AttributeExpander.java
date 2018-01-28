package com.nucleus.geometry;

import java.nio.FloatBuffer;

import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.vecmath.AxisAngle;
import com.nucleus.vecmath.Transform;

/**
 * Copy and expand attribute data from source to destination.
 * This can for instance be used to expand quad/sprite data into destination mesh.
 * Use this for CPU based attribute mapping
 *
 */
public class AttributeExpander implements Consumer {

    float[] data;
    int dataSize;
    int vertices;
    int multiplier;
    int sourceOffset = 0;
    int destOffset = 0;
    PropertyMapper mapper;
    AttributeBuffer buffer;

    /**
     * 
     * @param mapper
     * @param data
     * @param dataSize
     * @param vertices
     * @param multiplier
     */
    public AttributeExpander(PropertyMapper mapper, float[] data, int dataSize, int vertices, int multiplier) {
        this.mapper = mapper;
        this.data = data;
        this.dataSize = dataSize;
        this.vertices = vertices;
        this.multiplier = multiplier;
    }

    @Override
    public void updateAttributeData() {
        int source = sourceOffset;
        int dest = destOffset;
        FloatBuffer destination = buffer.getBuffer();
        for (int i = 0; i < vertices; i++) {
            for (int expand = 0; expand < multiplier; expand++) {
                destination.position(dest);
                destination.put(data, source, mapper.attributesPerVertex);
                dest += mapper.attributesPerVertex;
            }
            source += dataSize;
        }
        buffer.setDirty(true);
    }

    @Override
    public void bindAttributeBuffer(AttributeBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * 
     * @param vertex
     * @param transform
     * @param frame
     */
    public void setData(int vertex, Transform transform) {
        int offset = dataSize * vertex;
        float[] translate = transform.getTranslate();
        if (translate != null) {
            data[offset + mapper.translateOffset] = translate[0];
            data[offset + mapper.translateOffset + 1] = translate[1];
            data[offset + mapper.translateOffset + 2] = translate[2];
        }
        if (transform.getAxisAngle() != null) {
            float[] axisangle = transform.getAxisAngle().getValues();
            float angle = axisangle[AxisAngle.ANGLE];
            data[offset + mapper.rotateOffset] = axisangle[AxisAngle.X] * angle;
            data[offset + mapper.rotateOffset + 1] = axisangle[AxisAngle.Y] * angle;
            data[offset + mapper.rotateOffset + 2] = axisangle[AxisAngle.Z] * angle;
        }
        float[] scale = transform.getScale();
        if (scale != null) {
            data[offset + mapper.scaleOffset] = scale[0];
            data[offset + mapper.scaleOffset + 1] = scale[1];
            data[offset + mapper.scaleOffset + 2] = scale[2];
        }
    }

}
