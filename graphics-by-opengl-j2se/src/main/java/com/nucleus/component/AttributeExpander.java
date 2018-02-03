package com.nucleus.component;

import java.nio.FloatBuffer;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.vecmath.AxisAngle;
import com.nucleus.vecmath.Transform;

/**
 * Copy and expand attribute data from source to destination.
 * This can for instance be used to expand quad/sprite data into destination mesh.
 *
 */
public class AttributeExpander implements Consumer {

    ComponentBuffer data;
    int multiplier;
    int sourceOffset = 0;
    int destOffset = 0;
    PropertyMapper mapper;
    AttributeBuffer buffer;

    /**
     * 
     * @param mapper
     * @param data
     * @param multiplier
     */
    public AttributeExpander(PropertyMapper mapper, ComponentBuffer data, int multiplier) {
        this.mapper = mapper;
        this.data = data;
        this.multiplier = multiplier;
    }

    @Override
    public void updateAttributeData() {
        int source = sourceOffset;
        int dest = destOffset;
        FloatBuffer destination = buffer.getBuffer();
        for (int i = 0; i < data.entityCount; i++) {
            for (int expand = 0; expand < multiplier; expand++) {
                destination.position(dest);
                // destination.put(data, source, mapper.attributesPerVertex);
                dest += mapper.attributesPerVertex;
                throw new IllegalArgumentException("Not implemented");
            }
            source += data.sizePerEntity;
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
        int offset = data.sizePerEntity * vertex;
        float[] translate = transform.getTranslate();
        if (translate != null) {
            data.put(vertex, mapper.translateOffset, translate, 0, 3);
        }
        if (transform.getAxisAngle() != null) {
            float[] axisangle = transform.getAxisAngle().getValues();
            float angle = axisangle[AxisAngle.ANGLE];
            data.put(vertex, mapper.rotateOffset, translate, 0, 3);
        }
        float[] scale = transform.getScale();
        if (scale != null) {
            data.put(vertex, mapper.scaleOffset, translate, 0, 3);
        }
    }

}
