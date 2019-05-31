package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.Assets;

/**
 * A dynamic texture, for instance attached as framebuffer when render to texture.
 * Just like a {@link Texture2D} but can set clear color when texture is created.
 * Use {@link Assets#getIdReference(Texture2D)} to fetch a texture that has been attached to rendertarget.
 * Do not set texture format/type in this object since they are defined when texture created as target.
 *
 */
public class DynamicTexture2D extends Texture2D {

    public final static String INITCOLOR = "initcolor";

    @SerializedName(INITCOLOR)
    private float[] initcolor;

    /**
     * Returns the init color if set.
     * 
     * @return Color to init texture to, or null
     */
    public float[] getInitColor() {
        return initcolor;
    }

}
