package com.nucleus.io;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.geometry.Mesh;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.texturing.UVAtlas;
import com.nucleus.texturing.UVTexture2D;

/**
 * Definition of all resource references (for a scene)
 * The objects held here are the references to data to include, they do not contain the data themself.
 * Ie the Texture2D object are contain the reference to image and texture parameters, not the loaded texture.
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
     * Copies the resources (definitions) from the source into this class
     * 
     * @param source
     */
    public void copy(ResourcesData source) {
        copyMeshes(source.mesh);
        copyTextures(source.texture2D);
        copyTiledTextures(source.tiledTexture2D);
        copyUVTextures(source.uvTexture2D);
    }

    /**
     * Copies the mesh definitions into this class, adding to the list of available meshes
     * 
     * @param meshes
     */
    protected void copyMeshes(ArrayList<Mesh> meshes) {
        for (Mesh m : meshes) {
            mesh.add(new Mesh(m));
        }
    }

    /**
     * Copies the texture definitions into this class, adding to the list of available textures
     * If texture with same id already exist then nothing is added.
     * 
     * @param textures List of texture objects to add to this class
     */
    protected void copyTextures(ArrayList<Texture2D> textures) {
        for (Texture2D t : textures) {
            addTexture(t);
        }
    }

    /**
     * Copies the texture definitions into this class, adding to the list of available textures.
     * If texture with same id already exist then nothing is added
     * 
     * @param textures List of texture objects to add to this class
     */
    protected void copyTiledTextures(ArrayList<TiledTexture2D> textures) {
        for (TiledTexture2D t : textures) {
            addTexture(t);
        }
    }

    /**
     * Copies the texture definitions into this class, adding to the list of available textures.
     * If texture with same id already exist then nothing is added
     * 
     * @param textures List of texture objects to add to this class
     */
    protected void copyUVTextures(ArrayList<UVTexture2D> textures) {
        for (UVTexture2D t : textures) {
            addTexture(t);
        }
    }

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
     * Returns the (first) texture2d, or tiledtexture2d definition with matching id, or null if not found.
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
     * Adds a number of texture definitions to the resource data, if a texture with matching ID already
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
        if (getTexture2D(texture.getId()) != null) {
            System.out.println("Not adding texture, id already present: " + texture.getId());
            return;
        }
        if (texture instanceof TiledTexture2D) {
            tiledTexture2D.add((TiledTexture2D) texture);
        } else if (texture instanceof UVTexture2D) {
            uvTexture2D.add((UVTexture2D) texture);
        } else {
            texture2D.add(texture);
        }
    }

    /**
     * Adds the mesh definition to list of meshes, if a mesh with same id already exist then the mesh is not added
     * 
     * @param mesh
     */
    public void addMesh(Mesh mesh) {
        if (getMesh(mesh.getId()) != null) {
            System.out.println("Not adding mesh, id already present: " + mesh.getId());
            return;
        }
        this.mesh.add(mesh);
    }

    /**
     * Returns the first Mesh definition with the specified id.
     * 
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
