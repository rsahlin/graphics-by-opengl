package com.nucleus.component;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.shader.ShaderProperty.PropertyMapper;

/**
 * Copy and expand attribute data from source to destination.
 * This can for instance be used to expand quad/sprite data into destination mesh.
 *
 */
public abstract class AttributeExpander implements Consumer {

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
        this.multiplier = multiplier;
        tempData = new float[data.sizePerEntity];
    }

    @Override
    public void bindAttributeBuffer(AttributeBuffer buffer) {
        this.buffer = buffer;
    }

}
