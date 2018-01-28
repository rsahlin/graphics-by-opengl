package com.nucleus.geometry;

import java.nio.FloatBuffer;

import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureType;
import com.nucleus.texturing.UVAtlas;
import com.nucleus.texturing.UVTexture2D;

public class QuadExpander extends AttributeExpander {

    /**
     * Storage for 4 UV components
     */
    private transient float[][] frames;
    private transient Texture2D texture;

    public QuadExpander(Texture2D texture, PropertyMapper mapper, float[] data, int dataSize, int quads,
            int multiplier) {
        super(mapper, data, dataSize, quads, multiplier);
        this.texture = texture;
        this.frames = new float[vertices][2 * 4];
    }

    /**
     * 
     * @param vertex
     * @param x
     * @param y
     * @param z
     * @param rotateZ
     * @param scaleX
     * @param scaleY
     * @param scaleZ
     * @param frame
     */
    public void setData(int quad, float x, float y, float z, float rotateZ, float scaleX, float scaleY, float scaleZ,
            int frame) {
        int offset = dataSize * quad;
        data[offset + mapper.translateOffset] = x;
        data[offset + mapper.translateOffset + 1] = y;
        data[offset + mapper.translateOffset + 2] = z;
        data[offset + mapper.rotateOffset + 2] = rotateZ;
        data[offset + mapper.scaleOffset] = scaleX;
        data[offset + mapper.scaleOffset + 1] = scaleY;
        data[offset + mapper.scaleOffset + 2] = scaleZ;
        setFrame(quad, frame);
    }

    @Override
    public void updateAttributeData() {
        if (texture.getTextureType() == TextureType.UVTexture2D) {
            int source = sourceOffset;
            int dest = destOffset;
            FloatBuffer destination = buffer.getBuffer();
            int uvIndex = 0;
            for (int i = 0; i < vertices; i++) {
                uvIndex = 0;
                for (int expand = 0; expand < multiplier; expand++) {
                    data[source + mapper.frameOffset] = frames[i][uvIndex++];
                    data[source + mapper.frameOffset + 1] = frames[i][uvIndex++];
                    destination.put(data, source, mapper.attributesPerVertex);
                    dest += mapper.attributesPerVertex;
                }
                source += dataSize;
            }
            buffer.setDirty(true);
        } else {
            super.updateAttributeData();
        }
    }

    public void setColor(int quad, float[] color) {
        int offset = dataSize * quad;
        data[offset + mapper.colorOffset] = color[0];
        data[offset + mapper.colorOffset + 1] = color[1];
        data[offset + mapper.colorOffset + 2] = color[2];
        data[offset + mapper.colorOffset + 3] = color[3];

    }

    public void setFrame(int quad, int frame) {
        int offset = dataSize * quad;
        if (texture.textureType == TextureType.TiledTexture2D) {
            data[offset + mapper.frameOffset] = frame;
        } else if (texture.textureType == TextureType.UVTexture2D) {
            setFrame(quad, ((UVTexture2D) texture).getUVAtlas(), frame);
        }
    }

    void setFrame(int quad, UVAtlas uvAtlas, int frame) {
        // Fetch the current frames UV into buffer
        uvAtlas.getUVFrame(frame, frames[quad], 0);
    }

}
