package com.nucleus.component;

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

    /**
     * 
     * @param texture
     * @param mapper
     * @param data
     * @param multiplier
     */
    public QuadExpander(Texture2D texture, PropertyMapper mapper, ComponentBuffer data, int multiplier) {
        super(mapper, data, multiplier);
        this.texture = texture;
        this.frames = new float[data.entityCount][2 * 4];
    }

    @Override
    public void updateAttributeData() {
        if (texture.getTextureType() == TextureType.UVTexture2D) {
            int uvIndex = 0;
            buffer.getBuffer().position(0);
            for (int i = 0; i < data.entityCount; i++) {
                uvIndex = 0;
                for (int expand = 0; expand < multiplier; expand++) {
                    // Store the UV for the vertex
                    data.put(i, mapper.frameOffset, frames[i], uvIndex, 2);
                    data.get(i, buffer);
                    uvIndex += 2;
                }
            }
            buffer.setDirty(true);
        } else {
            super.updateAttributeData();
        }
    }

    public void setColor(int quad, float[] color) {
        data.put(quad, mapper.colorOffset, color, 0, 4);
    }

    public void setFrame(int quad, int frame) {
        if (texture.textureType == TextureType.TiledTexture2D) {
            // TODO - this is highly unoptimized
            data.put(quad, mapper.frameOffset, new float[] { frame }, 0, 1);
        } else if (texture.textureType == TextureType.UVTexture2D) {
            setFrame(quad, ((UVTexture2D) texture).getUVAtlas(), frame);
        }
    }

    void setFrame(int quad, UVAtlas uvAtlas, int frame) {
        // Fetch the current frames UV into buffer, this will then be used next update when data is expanded in
        // updateAttributeData()
        uvAtlas.getUVFrame(frame, frames[quad], 0);
    }

}
