package com.nucleus.component;

import com.nucleus.SimpleLogger;
import com.nucleus.common.Constants;
import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.geometry.Mesh;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureType;
import com.nucleus.texturing.UVAtlas;
import com.nucleus.texturing.UVTexture2D;
import com.nucleus.vecmath.AxisAngle;
import com.nucleus.vecmath.Transform;

/**
 * Sprite / Quad expander this is the connection between the source (entity) buffer, that keeps track of one set of
 * variables for an on screen quad, and the destination buffer that tracks 4 set of attribute values.
 * The source and destination shall have a one to one mapping in variable offsets, eg to expand the source data it shall
 * be enough to copy it into 4 places (one for each vertex)
 * If expander is CPU based methods to set data shall update both source and destination, meaning that
 * {@link #updateAttributeData(NucleusRenderer)} only needs to copy
 * the destination data into the attribute buffer.
 *
 *
 */
public class CPUQuadExpander extends AttributeExpander {

    /**
     * The source data, for instance entity/sprite data
     * Data shall be written here and expanded into AttributeBuffer when {@link #updateAttributeData(NucleusRenderer)}
     * is called.
     */
    protected CPUComponentBuffer source;
    protected CPUComponentBuffer destination;
    protected float[] sourceData;
    protected float[] destinationData;

    private transient Texture2D texture;
    /**
     * Only used for uniform block no uniform block in shader
     */
    private transient float[][] uvData;
    /**
     * Used as temp entity data
     */
    private transient float[] tempData;

    private int sizePerVertex;

    /**
     * 
     * @param spriteMesh
     * @param mapper
     * @param data
     */
    public CPUQuadExpander(Mesh spriteMesh, PropertyMapper mapper, CPUComponentBuffer source,
            CPUComponentBuffer destination) {
        super(mapper, destination, 4);
        this.source = source;
        this.sourceData = source.data;
        this.destination = destination;
        this.destinationData = destination.data;
        this.texture = spriteMesh.getTexture(Texture2D.TEXTURE_0);
        this.sizePerVertex = mapper.attributesPerVertex;
        if (texture.getTextureType() == TextureType.UVTexture2D) {
            // If mesh has block buffers then frames will be in uniform block - do not copy here
            if (spriteMesh.getBlockBuffers() == null) {
                SimpleLogger.d(getClass(), "No uniform block - does renderer not have support for GLES3 or above?");
                copyUVAtlas(((UVTexture2D) texture).getUVAtlas());
                tempData = new float[mapper.attributesPerVertex];
            }
        }
    }

    private void copyUVAtlas(UVAtlas uvAtlas) {
        int frames = uvAtlas.getFrameCount();
        uvData = new float[frames][];
        for (int i = 0; i < frames; i++) {
            uvData[i] = new float[8];
            uvAtlas.getUVFrame(i, uvData[i], 0);
        }
    }

    @Override
    public void updateAttributeData(NucleusRenderer renderer) {
        // Use special case if shader does not support uniform block where the frames are.
        if (texture.getTextureType() == TextureType.UVTexture2D && tempData != null) {
            int uvIndex = 0;
            int frame;
            buffer.setBufferPosition(0);
            for (int i = 0; i < source.getEntityCount(); i++) {
                uvIndex = 0;
                frame = (int) tempData[mapper.frameOffset];
                // data.get(i, tempData);
                for (int expand = 0; expand < multiplier; expand++) {
                    // Store the UV for the vertex
                    tempData[mapper.frameOffset] = uvData[frame][uvIndex++];
                    tempData[mapper.frameOffset + 1] = uvData[frame][uvIndex++];
                    buffer.put(tempData);
                }
            }
        } else {
            // Move from data to buffer
            buffer.setBufferPosition(0);
            buffer.put(destinationData);
        }
    }

    /**
     * Sets the color
     * 
     * @param quad
     * @param color
     */
    public void setColor(int quad, float[] color) {
        int index = quad * source.sizePerEntity + mapper.colorOffset;
        int destIndex = quad * destination.sizePerEntity + mapper.colorOffset;
        sourceData[index++] = color[0];
        sourceData[index++] = color[1];
        sourceData[index++] = color[2];
        sourceData[index] = color[3];
        for (int i = 0; i < 4; i++) {
            destinationData[destIndex++] = color[0];
            destinationData[destIndex++] = color[1];
            destinationData[destIndex++] = color[2];
            destinationData[destIndex] = color[3];
            destIndex += sizePerVertex - 3;
        }
    }

    /**
     * Sets the frame number
     * 
     * @param quad
     * @param frame
     */
    public final void setFrame(int quad, int frame) {
        if (mapper.frameOffset != Constants.NO_VALUE) {
            int index = quad * source.sizePerEntity + mapper.frameOffset;
            sourceData[index] = frame;
            index = quad * destination.sizePerEntity + mapper.frameOffset;
            destinationData[index] = frame;
            index += sizePerVertex;
            destinationData[index] = frame;
            index += sizePerVertex;
            destinationData[index] = frame;
            index += sizePerVertex;
            destinationData[index] = frame;
        }
    }

    /**
     * Sets the position
     * 
     * @param quad
     * @param x
     * @param y
     */
    public final void setPosition(int quad, float x, float y) {
        int index = quad * source.sizePerEntity + mapper.translateOffset;
        sourceData[index] = x;
        sourceData[index + 1] = y;
        index = quad * destination.sizePerEntity + mapper.translateOffset;
        destinationData[index] = x;
        destinationData[index + 1] = y;
        index += sizePerVertex;
        destinationData[index] = x;
        destinationData[index + 1] = y;
        index += sizePerVertex;
        destinationData[index] = x;
        destinationData[index + 1] = y;
        index += sizePerVertex;
        destinationData[index] = x;
        destinationData[index + 1] = y;
    }

    /**
     * Sets the transform in the source and destination buffer buffer.
     * This method will call {@link #expandQuadData(int)} after setting the source data
     * 
     * @param quad
     * @param transform
     */
    public final void setData(int quad, Transform transform) {
        float[] translate = transform.getTranslate();
        int start = quad * source.sizePerEntity;
        if (translate != null) {
            int index = start + mapper.translateOffset;
            for (int i = 0; i < 4; i++) {
                sourceData[index++] = translate[0];
                sourceData[index++] = translate[1];
                sourceData[index] = translate[2];
                index += sizePerVertex - 2;
            }
        }
        if (transform.getAxisAngle() != null) {
            int index = start + mapper.rotateOffset;
            float[] axisangle = transform.getAxisAngle().getValues();
            float angle = axisangle[AxisAngle.ANGLE];
            for (int i = 0; i < 4; i++) {
                sourceData[index++] = axisangle[AxisAngle.X] * angle;
                sourceData[index++] = axisangle[AxisAngle.Y] * angle;
                sourceData[index] = axisangle[AxisAngle.Z] * angle;
                index += sizePerVertex - 2;
            }
        }
        float[] scale = transform.getScale();
        if (scale != null) {
            int index = start + mapper.scaleOffset;
            for (int i = 0; i < 4; i++) {
                sourceData[index++] = scale[0];
                sourceData[index++] = scale[1];
                sourceData[index] = scale[2];
                index += sizePerVertex - 2;
            }
        }
        expandQuadData(quad);
    }

    /**
     * Sets all the data for the specified quad, the caller shall make sure the data is indexed using the appropriate
     * mapper.
     * 
     * @param quad
     * @param data
     */
    public final void setData(int quad, float[] data) {
        int index = quad * source.sizePerEntity;
        System.arraycopy(data, 0, sourceData, index, data.length);
        expandQuadData(quad);
    }

    /**
     * Sets the xyz axis values for the transform, use this method when initializing
     * This will set the transform in the source buffer.
     * 
     * @param quad
     * @param transform 3 axis translate, rotate and scale values
     */
    public final void setTransform(int quad, float[] transform) {
        int index = quad * source.sizePerEntity;
        sourceData[index + mapper.translateOffset] = transform[0];
        sourceData[index + 1 + mapper.translateOffset] = transform[1];
        sourceData[index + 2 + mapper.translateOffset] = transform[2];

        sourceData[index + mapper.rotateOffset] = transform[3];
        sourceData[index + 1 + mapper.rotateOffset] = transform[4];
        sourceData[index + 2 + mapper.rotateOffset] = transform[5];

        sourceData[index + mapper.scaleOffset] = transform[6];
        sourceData[index + 1 + mapper.scaleOffset] = transform[7];
        sourceData[index + 2 + mapper.scaleOffset] = transform[8];
        expandQuadData(quad);
    }

    /**
     * Expands the data for one quad, from source into destination for all 4 vertices of the quad.
     * 
     * @param quad
     * @param srcData
     * @param offset
     * @return
     */
    public final void expandQuadData(int quad) {
        int index = quad * destination.sizePerEntity;
        int sourceIndex = quad * source.sizePerEntity;
        System.arraycopy(sourceData, sourceIndex, destinationData, index, sizePerVertex);
        index += sizePerVertex;
        System.arraycopy(sourceData, sourceIndex, destinationData, index, sizePerVertex);
        index += sizePerVertex;
        System.arraycopy(sourceData, sourceIndex, destinationData, index, sizePerVertex);
        index += sizePerVertex;
        System.arraycopy(sourceData, sourceIndex, destinationData, index, sizePerVertex);
        index += sizePerVertex;
    }

}
