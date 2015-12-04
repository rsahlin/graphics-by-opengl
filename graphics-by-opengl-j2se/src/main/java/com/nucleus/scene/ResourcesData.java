package com.nucleus.scene;

import com.nucleus.texturing.Texture2DData;
import com.nucleus.texturing.TiledTexture2DData;

/**
 * Definition of all resources (for a scene)
 * 
 * @author Richard Sahlin
 *
 */
public class ResourcesData {

    private Texture2DData[] texture2DData;
    private TiledTexture2DData[] tiledTexture2DData;

    /**
     * Returns the defined texture objects
     * 
     * @return
     */
    public Texture2DData[] getTexture2DData() {
        return texture2DData;
    }

    /**
     * Returns the defined tiled texture objects
     * 
     * @return
     */
    public TiledTexture2DData[] getTiledTexture2DData() {
        return tiledTexture2DData;
    }

    /**
     * Returns the (first) texture2d, or tiledtexture2d data with matching id, or null if not found.
     * 
     * @param id
     * @return
     */
    public Texture2DData getTexture2DData(String id) {
        for (Texture2DData t : texture2DData) {
            if (id.equals(t.getId())) {
                return t;
            }
        }
        for (TiledTexture2DData tt : tiledTexture2DData) {
            if (id.equals(tt.getId())) {
                return tt;
            }
        }
        return null;
    }

}
