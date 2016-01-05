package com.nucleus.io;

import java.util.ArrayList;

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

    public final static String RESOURCE_ALREADY_EXIST = "Resource already present with id: ";

    @SerializedName("texture2D")
    private ArrayList<Texture2D> texture2D = new ArrayList<Texture2D>();
    @SerializedName("tiledTexture2D")
    private ArrayList<TiledTexture2D> tiledTexture2D = new ArrayList<TiledTexture2D>();

    /**
     * Returns the defined texture objects
     * 
     * @return
     */
    public Texture2D[] getTexture2DData() {
        return (Texture2D[]) texture2D.toArray();
    }

    /**
     * Returns the defined tiled texture objects
     * 
     * @return
     */
    public TiledTexture2D[] getTiledTexture2DData() {
        return (TiledTexture2D[]) tiledTexture2D.toArray();
    }

    /**
     * Returns the (first) texture2d, or tiledtexture2d data with matching id, or null if not found.
     * 
     * @param id
     * @return Texture with specified id or null if not found, or if there are no textures
     */
    public Texture2D getTexture2DData(String id) {
        if (texture2D != null) {
            for (Texture2D t : texture2D) {
                if (id.equals(t.getId())) {
                    return t;
                }
            }
        }
        if (tiledTexture2D != null) {
            for (TiledTexture2D tt : tiledTexture2D) {
                if (id.equals(tt.getId())) {
                    return tt;
                }
            }
        }
        return null;
    }

    /**
     * Adds a number of textures to the resource data, if a texture with matching ID already
     * exist then nothing is added.
     * 
     * @param textures The textures to add, if texture with same id is not present
     */
    public void addTextures(Texture2D[] textures) {
        for (Texture2D texture : textures) {
            if (getTexture2DData(texture.getId()) == null) {
                addTexture(texture);
            }
        }
    }

    /**
     * Adds a texture to the resource data, if a texture with matching ID already
     * exist then nothing is added.
     * 
     * @param texture
     */
    public void addTexture(Texture2D texture) {
        if (texture instanceof TiledTexture2D) {
            tiledTexture2D.add((TiledTexture2D) texture);
        } else {
            texture2D.add(texture);
        }
    }

}
