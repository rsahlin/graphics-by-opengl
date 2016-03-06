package com.nucleus.io;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.geometry.Mesh;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.texturing.UVAtlas;
import com.nucleus.texturing.UVTexture2D;

/**
 * Definition of all resources (for a scene)
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class ResourcesData {

    public final static String RESOURCE_ALREADY_EXIST = "Resource already present with id: ";

    @SerializedName("uvAtlas")
    private ArrayList<UVAtlas> uvAtlas = new ArrayList<UVAtlas>();
    @SerializedName("texture2D")
    private ArrayList<Texture2D> texture2D = new ArrayList<Texture2D>();
    @SerializedName("tiledTexture2D")
    private ArrayList<TiledTexture2D> tiledTexture2D = new ArrayList<TiledTexture2D>();
    @SerializedName("uvTexture2D")
    private ArrayList<UVTexture2D> uvTexture2D = new ArrayList<UVTexture2D>();
    @SerializedName("mesh")
    private ArrayList<Mesh> mesh = new ArrayList<Mesh>();

    /**
     * Returns the defined texture objects
     * 
     * @return
     */
    public Texture2D[] getTexture2D() {
        return (Texture2D[]) texture2D.toArray();
    }

    /**
     * Returns the defined tiled texture objects
     * 
     * @return
     */
    public TiledTexture2D[] getTiledTexture2D() {
        return (TiledTexture2D[]) tiledTexture2D.toArray();
    }

    /**
     * Returns the (first) texture2d, or tiledtexture2d data with matching id, or null if not found.
     * 
     * @param id
     * @return Texture with specified id or null if not found, or if there are no textures
     */
    public Texture2D getTexture2D(String id) {
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
        if (uvTexture2D != null) {
            for (UVTexture2D uv : uvTexture2D) {
                if (id.equals(uv.getId())) {
                    return uv;
                }
            }
        }
        return null;
    }

    /**
     * Returns the (first) uvAtlas with matching id, or null if not found
     * 
     * @param id Id of uvatlas to return.
     * @return UVAtlas with specified id or null if not found.
     */
    public UVAtlas getUVAtlas(String id) {
        for (UVAtlas uva : uvAtlas) {
            if (uva.getId().equals(id)) {
                return uva;
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
            if (getTexture2D(texture.getId()) == null) {
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
        } else if (texture instanceof UVTexture2D) {
            uvTexture2D.add((UVTexture2D) texture);
        } else {
            texture2D.add(texture);
        }
    }

    /**
     * Returns the first instance of a Mesh with the specified id.
     * @param id
     * @return The mesh or null if not found.
     */
    public Mesh getMesh(String id) {
        for (Mesh m : mesh) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        return null;
    }

}
