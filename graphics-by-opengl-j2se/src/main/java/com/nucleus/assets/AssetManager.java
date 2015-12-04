package com.nucleus.assets;

import java.io.IOException;
import java.util.Hashtable;

import com.nucleus.io.ExternalReference;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2DData;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureSetup;

/**
 * Loading and unloading assets, mainly textures.
 * 
 * @author Richard Sahlin
 *
 */
public class AssetManager {

    protected static AssetManager assetManager = null;

    private final static String NO_TEXTURE_SOURCE_ERROR = "No texture source for id: ";

    /**
     * Store textures using the source image name.
     */
    private Hashtable<String, Texture2D> textures = new Hashtable<String, Texture2D>();
    /**
     * Use to convert from object id (texture reference) to name of source (file)
     */
    private Hashtable<String, ExternalReference> sourceNames = new Hashtable<String, ExternalReference>();

    /**
     * Hide the constructor
     */
    private AssetManager() {
    }

    public static AssetManager getInstance() {
        if (assetManager == null) {
            assetManager = new AssetManager();
        }
        return assetManager;
    }

    public interface Asset {
        /**
         * Loads an instance of the asset into memory, after this method returns the asset SHALL be ready to be used.
         * 
         * @param source The source of the object, it is up to implementations to decide what sources to support.
         * For images the normal usecase is InputStream
         * 
         * @return The id of the asset, this is a counter starting at 1 and increasing.
         * @throws IOException If there is an exception reading from the stream.
         */
        public int load(Object source) throws IOException;

        /**
         * Releases the asset and all allocated memory, after this method returns all memory and objects shall be
         * released.
         */
        public void destroy();
    }

    /**
     * Returns the texture, if the texture has not been loaded it will be loaded and stored in the assetmanager.
     * 
     * @param renderer
     * @param source
     * @return The texture
     * @throws IOException
     */
    public Texture2D getTexture(NucleusRenderer renderer, TextureSetup source) throws IOException {

        Texture2D texture = textures.get(source.getSourceName());
        if (texture != null) {
            return texture;
        }

        texture = TextureFactory.createTexture(renderer.getGLES(), renderer.getImageFactory(), source);
        textures.put(source.getSourceName(), texture);
        ExternalReference ref = new ExternalReference(source.getSourceName());
        ref.setId(source.getId());
        sourceNames.put(source.getId(), ref);
        return texture;
    }

    /**
     * Returns the texture, if the texture has not been loaded it will be loaded and stored in the assetmanager.
     * 
     * @param renderer
     * @param source
     * @return The texture
     * @throws IOException
     */
    public Texture2D getTexture(NucleusRenderer renderer, Texture2DData source) throws IOException {

        ExternalReference ref = source.getExternalReference();
        Texture2D texture = textures.get(ref.getSource());
        if (texture != null) {
            return texture;
        }
        texture = TextureFactory.createTexture(renderer.getGLES(), renderer.getImageFactory(), source);
        ref.setId(source.getId());
        textures.put(ref.getSource(), texture);
        sourceNames.put(source.getId(), ref);
        return texture;
    }

    /**
     * Returns the source reference for the texture with the specified id, this can be used to fetch texture
     * source name from a texture reference/id.
     * If the source reference cannot be found it is considered an error and an exception is thrown.
     * 
     * @param Id
     * @return The source (file) reference or null if not found.
     * @throws IllegalArgumentException If a texture source could not be found for the Id.
     */
    public ExternalReference getSourceReference(String Id) {
        ExternalReference ref = sourceNames.get(Id);
        if (ref == null) {
            // Horrendous error - cannot export data!
            // TODO Is there a way to recover?
            throw new IllegalArgumentException(NO_TEXTURE_SOURCE_ERROR + Id);
        }
        return ref;
    }

}
