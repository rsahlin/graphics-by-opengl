package com.nucleus.component;

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
        buffer.getBuffer().position(0);
        for (int i = 0; i < data.entityCount; i++) {
            for (int expand = 0; expand < multiplier; expand++) {
                data.get(i, buffer);
            }
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
            data.put(vertex, mapper.scaleOffset, scale, 0, 3);
        }
    }

}
