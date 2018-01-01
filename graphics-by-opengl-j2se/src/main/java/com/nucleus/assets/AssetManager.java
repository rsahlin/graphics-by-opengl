package com.nucleus.assets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

import com.nucleus.SimpleLogger;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Image.ImageFormat;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureParameter.Parameter;
import com.nucleus.texturing.TextureType;

/**
 * Loading and unloading assets, mainly textures.
 * It should normally handle resources that are loaded separately from the main json file using an
 * {@link ExternalReference} eg data that does not fit within the main file.
 * 
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
    private HashMap<String, Texture2D> textures = new HashMap<>();
    /**
     * Use to convert from object id (texture reference) to name of source (file)
     */
    private Hashtable<String, ExternalReference> sourceNames = new Hashtable<String, ExternalReference>();

    private HashMap<String, ShaderProgram> programs = new HashMap<>();

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
     * If already has been loaded the loaded instance will be returned.
     * Treat textures as immutable object
     * 
     * @param renderer
     * @param ref
     * @return The texture
     * @throws IOException
     */
    public Texture2D getTexture(NucleusRenderer renderer, ExternalReference ref) throws IOException {
        String idRef = ref.getIdReference();
        if (idRef != null) {
            return getTexture(idRef);
        } else {
            return getTexture(renderer, TextureFactory.createTexture(ref));
        }
    }

    /**
     * If the reference texture is id reference and the reference is registered then the texture data is copied into
     * the reference, overwriting transient values and non-set (null) values.
     * 
     * @param reference
     */
    public void getIdReference(Texture2D reference) {
        if (reference != null && reference.getExternalReference().isIdReference()) {
            Texture2D source = getTexture(reference.getExternalReference().getIdReference());
            if (source == null) {
                throw new IllegalArgumentException("Could not find texture with id reference: "
                        + reference.getExternalReference().getIdReference());
            }
            TextureFactory.copyTextureInstance(source, reference);
        } else {
            // What should be done?
            SimpleLogger.d(getClass(), "Called getIdReference with null reference:");
        }
    }

    /**
     * Returns the texture for the rendertarget attachement, if not already create it will be created and stored in the
     * assetmanager with id taken from renderTarget and attachement
     * If already created the instance will be returned.
     * 
     * @param renderer
     * @param renderTarget The rendertarget that this texture is to be used for
     * @param attachement The attachement point for the texture
     * @return
     */
    public Texture2D createTexture(NucleusRenderer renderer, RenderTarget renderTarget, AttachementData attachement)
            throws GLException {
        if (renderTarget.getId() == null) {
            throw new IllegalArgumentException("RenderTarget must have an id");
        }
        Texture2D texture = textures.get(renderTarget.getAttachementId(attachement));
        if (texture == null) {
            // TODO - What values should be used when creating the texture?
            TextureType type = TextureType.Texture2D;
            RESOLUTION resolution = RESOLUTION.HD;
            int[] size = attachement.getSize();
            TextureParameter texParams = new TextureParameter(
                    new Parameter[] { Parameter.NEAREST, Parameter.NEAREST, Parameter.CLAMP,
                            Parameter.CLAMP });
            ImageFormat format = ImageFormat.valueOf(attachement.getFormat());
            texture = TextureFactory.createTexture(renderer.getGLES(), type, resolution, size, format, texParams);
            texture.setId(renderTarget.getAttachementId(attachement));
            textures.put(renderTarget.getAttachementId(attachement), texture);
        }
        return texture;
    }

    /**
     * Returns the texture, if the texture has not been loaded it will be loaded and stored in the assetmanager.
     * If already has been loaded the loaded instance will be returned.
     * Treat textures as immutable object
     * 
     * @param renderer
     * @param source The external ref is used to load a texture
     * @return The texture specifying the external reference to the texture to load and return.
     * @throws IOException
     */
    protected Texture2D getTexture(NucleusRenderer renderer, Texture2D source) throws IOException {
        /**
         * External ref for untextured needs to be "" so it can be stored and fetched.
         */
        if (source.getTextureType() == TextureType.Untextured) {
            source.setExternalReference(new ExternalReference(""));
        }
        ExternalReference ref = source.getExternalReference();
        if (ref == null) {
            throw new IllegalArgumentException("No external reference for texture id: " + source.getId());
        }
        String refId = ref.getIdReference();
        if (refId != null) {
            Texture2D texture = textures.get(refId);
            if (texture != null) {
                return texture;
            }
            textures.put(source.getExternalReference().getSource(), source);
            return source;
        } else {
            String refSource = ref.getSource();
            Texture2D texture = textures.get(refSource);
            if (texture == null) {
                long start = System.currentTimeMillis();
                // Texture not loaded
                texture = TextureFactory.createTexture(renderer.getGLES(), renderer.getImageFactory(), source);
                textures.put(refSource, texture);
                setExternalReference(texture.getId(), ref);
                FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_TEXTURE, " " + texture.getName(), start,
                        System.currentTimeMillis());
            }
            return texture;
        }
    }

    /**
     * Fetches a texture from map of registered textures
     * 
     * @param id Id of the texture, ususally the external source path.
     * @return The texture, or null if not registered
     */
    protected Texture2D getTexture(String id) {
        Texture2D texture = textures.get(id);
        if (texture == null) {
            return null;
        }
        return texture;

    }

    /**
     * Sets the external reference for the object id
     * 
     * @param id
     * @param externalReference
     * @throws IllegalArgumentException If a reference with the specified Id already has been set
     */
    private void setExternalReference(String id, ExternalReference externalReference) {
        if (sourceNames.containsKey(id)) {
            throw new IllegalArgumentException("Id already added as external reference:" + id);
        }
        sourceNames.put(id, externalReference);

    }

    /**
     * Returns the source reference for the object with the specified id, this can be used to fetch object
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

    /**
     * Returns a loaded and compiled shader program, if the program has not already been loaded and compiled it will be
     * added to AssetManager
     * 
     * @param renderer
     * @param program
     * @return An instance of the ShaderProgram that is loaded and compiled
     */
    public ShaderProgram getProgram(NucleusRenderer renderer, ShaderProgram program) {
        ShaderProgram compiled = programs.get(program.getKey());
        if (compiled != null) {
            return compiled;
        }
        long start = System.currentTimeMillis();
        renderer.createProgram(program);
        FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_SHADER, program.getClass().getSimpleName(), start,
                System.currentTimeMillis());
        programs.put(program.getKey(), program);
        return program;
    }

    /**
     * Removes all references and resources.
     * 
     * @param renderer
     */
    public void destroy(NucleusRenderer renderer) {
        SimpleLogger.d(getClass(), "destroy");
        deletePrograms(renderer.getGLES());
        deleteTextures(renderer.getGLES());
        programs.clear();
        sourceNames.clear();
        textures.clear();
    }

    private void deleteTextures(GLES20Wrapper wrapper) {
        if (textures.size() == 0) {
            return;
        }
        int[] texNames = new int[textures.size()];
        int i = 0;
        for (Texture2D texture : textures.values()) {
            texNames[i++] = texture.getName();
        }
        wrapper.glDeleteTextures(texNames);
    }

    private void deletePrograms(GLES20Wrapper wrapper) {
        for (ShaderProgram program : programs.values()) {
            wrapper.glDeleteProgram(program.getProgram());
        }
    }

}
