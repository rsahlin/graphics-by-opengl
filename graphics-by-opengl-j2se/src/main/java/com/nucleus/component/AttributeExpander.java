package com.nucleus.component;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.vecmath.AxisAngle;
import com.nucleus.vecmath.Transform;

/**
 * Copy and expand attribute data from source to destination.
 * This can for instance be used to expand quad/sprite data into destination mesh.
 *
 */
public class AttributeExpander implements Consumer {

    /**
     * The source data, for instance entity/sprite data
     * Data shall be written here and expanded into AttributeBuffer when {@link #updateAttributeData(NucleusRenderer)}
     * is called.
     */
    protected ComponentBuffer data;
    /**
     * The destination buffer, usually belonging to the mesh being rendered.
     * TODO - access to this buffer shall be limited to when rendering.
     * Otherwise native buffer operations will over/underflow since position may change.
     * 
     */
    protected AttributeBuffer buffer;
    protected int multiplier;
    protected int sourceOffset = 0;
    protected int destOffset = 0;
    protected PropertyMapper mapper;
    final float[] tempData;

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
        tempData = new float[data.sizePerEntity];
    }

    @Override
    public void updateAttributeData(NucleusRenderer renderer) {
        buffer.setBufferPosition(0);
        for (int i = 0; i < data.entityCount; i++) {
            data.get(i, tempData);
            for (int expand = 0; expand < multiplier; expand++) {
                buffer.put(tempData);
            }
        }
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
            float[] euler = new float[3];
            float angle = axisangle[AxisAngle.ANGLE];
            euler[0] = axisangle[AxisAngle.X] * angle;
            euler[1] = axisangle[AxisAngle.Y] * angle;
            euler[2] = axisangle[AxisAngle.Z] * angle;
            data.put(vertex, mapper.rotateOffset, euler, 0, 3);
        }
        float[] scale = transform.getScale();
        if (scale != null) {
            data.put(vertex, mapper.scaleOffset, scale, 0, 3);
        }
    }

}
