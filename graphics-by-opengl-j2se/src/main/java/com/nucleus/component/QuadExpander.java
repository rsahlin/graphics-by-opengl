package com.nucleus.component;

import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.shader.ComputeShader;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureType;
import com.nucleus.texturing.UVAtlas;
import com.nucleus.texturing.UVTexture2D;

/**
 * Sprite / Quad expander, same as AttributeExpander but adds methods for setting frame / color
 *
 */
public class QuadExpander extends AttributeExpander {

    protected static ComputeShader computeShader;

    /**
     * Storage for 4 UV components
     */
    private transient float[][] frames;
    private transient Texture2D texture;
    private transient float[][] uvData;
    private transient float[] entityData;

    /**
     * 
     * @param texture
     * @param mapper
     * @param data
     * @param multiplier
     */
    public QuadExpander(Texture2D texture, PropertyMapper mapper, ComponentBuffer data,
            int multiplier) {
        super(mapper, data, multiplier);
        this.texture = texture;
        this.frames = new float[data.entityCount][2 * 4];
        if (texture.getTextureType() == TextureType.UVTexture2D) {
            copyUVAtlas(((UVTexture2D) texture).getUVAtlas());
            entityData = new float[mapper.attributesPerVertex];
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
        if (computeShader == null) {
            loadShader(renderer);
        }
        if (texture.getTextureType() == TextureType.UVTexture2D) {
            int uvIndex = 0;
            int frame;
            buffer.getBuffer().position(0);
            float[] uv = new float[8];
            for (int i = 0; i < data.entityCount; i++) {
                uvIndex = 0;
                data.get(i, entityData);
                frame = (int) entityData[mapper.frameOffset];
                for (int expand = 0; expand < multiplier; expand++) {
                    // Store the UV for the vertex
                    data.put(i, mapper.frameOffset, uvData[frame], uvIndex, 2);
                    data.get(i, buffer);
                    uvIndex += 2;
                }
            }
            buffer.setDirty(true);
        } else {
            super.updateAttributeData(renderer);
        }
    }

    public void setColor(int quad, float[] color) {
        data.put(quad, mapper.colorOffset, color, 0, 4);
    }

    protected void loadShader(NucleusRenderer renderer) {
        // computeShader = (ComputeShader) AssetManager.getInstance().getProgram(renderer,
        // new ComputeShader(Shading.textured, ComputeVariables.values()));
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
