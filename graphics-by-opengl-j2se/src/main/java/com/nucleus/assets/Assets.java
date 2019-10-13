package com.nucleus.assets;

import java.io.IOException;

import com.nucleus.BackendException;
import com.nucleus.io.ExternalReference;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.Image;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.ImageFactory;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Format;
import com.nucleus.texturing.TextureParameter;

/**
 * Handles assets such as textures and programs/pipelines.
 * Clients shall only use this interface to handle assets - do not create textures/assets/programs by using
 * implementation classes.
 *
 */
public interface Assets {

    /**
     * Loads an image into several mip-map levels, the same image will be scaled to produce the
     * different mip-map levels.
     * If the value of {@link Texture2D#getLevels()} is > 1 and the texture parameters are set to support mipmap then
     * the mip levels are generated.
     * To automatically generate mipmaps, just set the texture parameters to support mipmap.
     * 
     * @param imageFactory ImageFactory to use when creating/scaling image
     * @param texture The texture source object
     * @return Array with an image for each mip-map level.
     */
    public BufferImage[] loadTextureMIPMAP(ImageFactory imageFactory, Texture2D texture);

    /**
     * Loads the assets needed for the glTF models. This will load binary buffers and texture images.
     * After this call the glTF is ready to be used
     * 
     * @param renderer
     * @param glTF
     * @throws IOException If there is an error reading binary buffers or images
     * @throws BackendException If there is an error creating textures, objects or programs
     */
    public void loadGLTFAssets(NucleusRenderer renderer, GLTF glTF) throws IOException, BackendException;

    /**
     * Returns the texture, if the texture has not been loaded it will be and stored in the assetmanager
     * Format will be RGBA and type UNSIGNED_BYTE
     * The texture will be uploaded to GL using the specified texture object name, if several mip-map levels are
     * supplied they will be used.
     * If the device current resolution is lower than the texture target resolution then a lower mip-map level is used.
     * 
     * @param renderer
     * @param imageFactory
     * @param id The id of the texture
     * @param externalReference
     * @param The image format
     * @param resolution
     * @param parameter
     * @param mipmap
     * @return A new texture object containing the texture image.
     */
    public Texture2D getTexture(NucleusRenderer renderer, ImageFactory imageFactory, String id,
            ExternalReference externalReference, Format format, RESOLUTION resolution, TextureParameter parameter,
            int mipmap);

    /**
     * Returns the texture, if the texture has not been loaded it will be loaded and stored in the assetmanager.
     * If already has been loaded the loaded instance will be returned.
     * Treat textures as immutable object
     * 
     * @param renderer
     * @param imageFactory
     * @param ref
     * @return The texture
     * @throws IOException
     * @throws IllegalArgumentException If renderer or ref is null
     */
    public Texture2D getTexture(NucleusRenderer renderer, ImageFactory imageFactory, ExternalReference ref)
            throws IOException;

    /**
     * If the Asset already has been loaded it is returned, otherwise AssetManager will load and return the GLTF asset.
     * This method will not load binary data (buffers) or images.
     * 
     * @param name
     * @return The loaded GLTF asset, without binary buffers and images loaded.
     * @throws IOException If there is an io exception reading the file, or it cannot be found.
     * @throws GLTFException If there is an error in the loaded gltf asset - this means it is malformed
     */
    public GLTF getGLTFAsset(String fileName) throws IOException, GLTFException;

    /**
     * Returns the texture for the rendertarget attachement, if not already created it will be created and stored in the
     * assetmanager with id taken from renderTarget and attachement
     * If already created the instance will be returned.
     * 
     * @param renderer
     * @param renderTarget The rendertarget that this texture is to be used for
     * @param attachement The attachement point for the texture
     * @param target The texture target
     * @return The texture2D object
     * @throws BackendException If there is an error creating the texture
     */
    public Texture2D createTexture(NucleusRenderer renderer, RenderTarget renderTarget, AttachementData attachement,
            int target) throws BackendException;

    /**
     * If the reference texture is id reference and the reference is registered then the texture data is copied into
     * the reference, overwriting transient values and non-set (null) values.
     * The reference texture must NOT set format/type/name/width/height of texture since these values are taken from
     * the target texture.
     * 
     * @param reference
     */
    public void getIdReference(Texture2D reference);

    /**
     * Returns a loaded and compiled graphics program.
     * If the pipeline shader has not already been loaded and compiled it will be loaded/compiled and linked then added
     * to Assets using shader as key.
     * 
     * 
     * @param renderer
     * @param shader
     * @return The shader, with pipeline
     * @throws BackendException If the pipeline could not be compiled or linked
     */
    public GraphicsShader getGraphicsPipeline(NucleusRenderer renderer, GraphicsShader shader)
            throws BackendException;

    /**
     * Deletes loaded gltf assets. This will delete binary buffers and texture images and then remove
     * the gltf asset from AssetManager.
     * Do not call this wile gltf model is in use - must call outside from render.
     * After this call the gltf asset must be loaded in order to be used again.
     * 
     * @param renderer
     * @param gltf
     * @throws BackendException If there is an error deleting resources
     */
    public void deleteGLTFAssets(NucleusRenderer renderer, GLTF gltf) throws BackendException;

    /**
     * Teardown and claenup all assets, removes all references and resources, call when the program is exiting.
     * Do not call any of the methods after calling this method
     * 
     * @param renderer
     */
    public void destroy(NucleusRenderer renderer);

    /**
     * Creates storage for an (empty) texture
     * 
     * @param texture The texture to create storage for
     * @param target
     * @throws BackendException
     */
    public void createTexture(Texture2D texture, int target) throws BackendException;

    /**
     * Deletes the texture Image
     * 
     * @param texture The texture image to delete, after calling this the image is not valid anymore
     */
    public void deleteTexture(Image image);

    /**
     * Deletes the textures
     * 
     * @param textures The texture images to delete, after calling this the textures are not valid anymore
     */
    public void deleteTextures(Texture2D[] textures);

}
