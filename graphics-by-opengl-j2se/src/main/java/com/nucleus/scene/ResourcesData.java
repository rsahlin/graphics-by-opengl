package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;

/**
 * Definition of all resources (for a scene)
 * 
 * @author Richard Sahlin
 *
 */
public class ResourcesData {

    @SerializedName("texture2D")
    private Texture2D[] texture2D;
    @SerializedName("tiledTexture2D")
    private TiledTexture2D[] tiledTexture2D;

    /**
     * Returns the defined texture objects
     * 
     * @return
     */
    public Texture2D[] getTexture2DData() {
        return texture2D;
    }

    /**
     * Returns the defined tiled texture objects
     * 
     * @return
     */
    public TiledTexture2D[] getTiledTexture2DData() {
        return tiledTexture2D;
    }

    /**
     * Returns the (first) texture2d, or tiledtexture2d data with matching id, or null if not found.
     * 
     * @param id
     * @return
     */
    public Texture2D getTexture2DData(String id) {
        for (Texture2D t : texture2D) {
            if (id.equals(t.getId())) {
                return t;
            }
        }
        for (TiledTexture2D tt : tiledTexture2D) {
            if (id.equals(tt.getId())) {
                return tt;
            }
        }
        return null;
    }

}
