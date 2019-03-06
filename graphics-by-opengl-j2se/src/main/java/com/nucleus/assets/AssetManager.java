package com.nucleus.assets;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.SimpleLogger;
import com.nucleus.io.ExternalReference;
import com.nucleus.io.gson.TextureDeserializer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.BufferObjectsFactory;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.gltf.Buffer;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.scene.gltf.Image;
import com.nucleus.scene.gltf.Material;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.scene.gltf.PBRMetallicRoughness;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Texture;
import com.nucleus.scene.gltf.Texture.TextureInfo;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.BaseImageFactory;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.BufferImage.ColorModel;
import com.nucleus.texturing.BufferImage.ImageFormat;
import com.nucleus.texturing.ImageFactory;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Format;
import com.nucleus.texturing.Texture2D.Type;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureParameter.Parameter;
import com.nucleus.texturing.TextureType;
import com.nucleus.texturing.TextureUtils;

/**
 * Loading and unloading assets, mainly textures - this is the main entrypoint for loading of textures and programs.
 * Clients shall only use this class - avoid calling methods to load assets (program/texture etc)
 * 
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
    private final static String NULL_PARAMETER = "Parameter is null: ";
    /**
     * Store textures using the source image name.
     */
    private HashMap<String, Texture2D> textures = new HashMap<>();

    /**
     * Loaded images that are used to create textures - clear when textures are created
     */
    private HashMap<String, BufferImage> images = new HashMap<>();

    private HashMap<String, ShaderProgram> programs = new HashMap<>();

    private HashMap<String, GLTF> gltfAssets = new HashMap<>();

    /**
     * Keep track of loaded texture objects by id
     */
    private static final Map<String, Texture2D> loadedTextures = new HashMap<>();

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
     * @param gles
     * @param imageFactory
     * @param ref
     * @return The texture
     * @throws IOException
     * @throws IllegalArgumentException If renderer or ref is null
     */
    public Texture2D getTexture(GLES20Wrapper gles, ImageFactory imageFactory, ExternalReference ref)
            throws IOException {
        if (gles == null || ref == null) {
            throw new IllegalArgumentException(NULL_PARAMETER + gles + ", " + null);
        }
        String idRef = ref.getIdReference();
        if (idRef != null) {
            return getTexture(idRef);
        } else {
            return getTexture(gles, imageFactory, createTexture(ref));
        }
    }

    /**
     * Returns the texture, if the texture has not been loaded it will be and stored in the assetmanager
     * Format will be RGBA and type UNSIGNED_BYTE
     * The texture will be uploaded to GL using the specified texture object name, if several mip-map levels are
     * supplied they will be used.
     * If the device current resolution is lower than the texture target resolution then a lower mip-map level is used.
     * 
     * @param gles
     * @param imageFactory
     * @param id The id of the texture
     * @param externalReference
     * @param resolution
     * @param parameter
     * @param mipmap
     * @return A new texture object containing the texture image.
     */
    public Texture2D getTexture(GLES20Wrapper gles, ImageFactory imageFactory, String id,
            ExternalReference externalReference, RESOLUTION resolution, TextureParameter parameter, int mipmap) {
        Texture2D source = TextureFactory.getInstance().createTexture(TextureType.Texture2D, id, externalReference,
                resolution, parameter, mipmap, Format.RGBA, Type.UNSIGNED_BYTE);
        internalCreateTexture(gles, imageFactory, source);
        return source;
    }

    /**
     * If the reference texture is id reference and the reference is registered then the texture data is copied into
     * the reference, overwriting transient values and non-set (null) values.
     * The reference texture must NOT set format/type/name/width/height of texture since these values are taken from
     * the target texture.
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
            // Make sure reference values not specified.
            if (reference.getFormat() != null || reference.getType() != null) {
                throw new IllegalArgumentException("Dynamic texture must not define format/type");
            }
            TextureFactory.getInstance().copyTextureInstance(source, reference);
        } else {
            throw new IllegalArgumentException("Called getIdReference with null reference, for texture " + reference);
        }
    }

    /**
     * Returns the texture for the rendertarget attachement, if not already created it will be created and stored in the
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
            TextureType type = TextureType.DynamicTexture2D;
            RESOLUTION resolution = RESOLUTION.HD;
            int[] size = attachement.getSize();
            TextureParameter texParams = new TextureParameter(
                    new Parameter[] { Parameter.NEAREST, Parameter.NEAREST, Parameter.CLAMP,
                            Parameter.CLAMP });
            ImageFormat format = attachement.getFormat();
            texture = createTexture(renderer.getGLES(), type, renderTarget.getId(), resolution, size, format,
                    texParams, GLES20.GL_TEXTURE_2D);
            texture.setId(renderTarget.getAttachementId(attachement));
            textures.put(renderTarget.getAttachementId(attachement), texture);
            SimpleLogger.d(getClass(), "Created texture: " + texture.toString());
        }
        return texture;
    }

    /**
     * Returns the texture, if the texture has not been loaded it will be loaded and stored in the assetmanager.
     * If already has been loaded the loaded instance will be returned.
     * Treat textures as immutable object
     * 
     * @param gles
     * @param imageFactory
     * @param source The external ref is used to load a texture
     * @return The texture specifying the external reference to the texture to load and return.
     * @throws IOException
     */
    protected Texture2D getTexture(GLES20Wrapper gles, ImageFactory imageFactory, Texture2D source) throws IOException {
        /**
         * External ref for untextured needs to be "" so it can be stored and fetched.
         */
        if (source.getTextureType() == TextureType.Untextured) {
            source.setExternalReference(new ExternalReference(""));
        }
        ExternalReference ref = source.getExternalReference();
        if (ref == null) {
            throw new IllegalArgumentException("External reference is null in texture with id: " + source.getId());
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
                texture = createTexture(gles, imageFactory, source);
                textures.put(refSource, texture);
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
     * Returns a loaded and compiled shader program, if the program has not already been loaded and compiled it will be
     * added to AssetManager using shader program and function.
     * Next time this method is called with the same shaderprogram and function the existing instance is returned.
     * 
     * @param gles
     * @param program
     * @return An instance of the ShaderProgram that is loaded and compiled
     * or linking the program.
     * @throws RuntimeException If the program could not be compiled or linked
     */
    public ShaderProgram getProgram(GLES20Wrapper gles, ShaderProgram program) {
        ShaderProgram compiled = programs.get(program.getKey());
        if (compiled != null) {
            return compiled;
        }
        try {
            long start = System.currentTimeMillis();
            program.createProgram(gles);
            FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_SHADER, program.getClass().getSimpleName(),
                    start,
                    System.currentTimeMillis());
            programs.put(program.getKey(), program);
            return program;
        } catch (GLException e) {
            throw new RuntimeException(e);
        }
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
        textures.clear();
    }

    private void deletePrograms(GLES20Wrapper wrapper) {
        for (ShaderProgram program : programs.values()) {
            wrapper.glDeleteProgram(program.getProgram());
        }
        programs.clear();
    }

    /**
     * If the Asset already has been loaded it is returned, otherwise AssetManager will load and return the GLTF asset.
     * This method will not load binary data (buffers) or images.
     * 
     * @param name
     * @return The loaded GLTF asset, without binary buffers and images loaded.
     * @throws IOException If there is an io exception reading the file, or it cannot be found.
     * @throws
     */
    public GLTF getGLTFAsset(String fileName) throws IOException, GLTFException {
        GLTF gltf = gltfAssets.get(fileName);
        if (gltf != null) {
            SimpleLogger.d(getClass(), "Returning already loaded gltf asset:" + fileName);
            return gltf;
        }
        SimpleLogger.d(getClass(), "Loading glTF asset:" + fileName);
        File f = new File(fileName);
        ClassLoader loader = getClass().getClassLoader();
        InputStream is = loader.getResourceAsStream(fileName);
        gltf = loadJSONAsset(f.getParent(), f.getName(), is);
        gltfAssets.put(gltf.getFilename(), gltf);
        return gltf;
    }

    /**
     * Loads the assets needed for the glTF models. This will load binary buffers and texture images.
     * After this call the glTF is ready to be used
     * 
     * @param gles
     * @param glTF
     * @throws IOException If there is an error reading binary buffers or images
     * @throws GLException If VBO creation fails
     */
    public void loadGLTFAssets(GLES20Wrapper gles, GLTF glTF) throws IOException, GLException {
        loadBuffers(glTF);
        loadTextures(gles, glTF, glTF.getMaterials());
        SimpleLogger.d(getClass(), "Loaded gltf assets");
        // Build TBN before creating VBOs
        // This can mean that a number of buffers needs to be created, for instance normal, tangent and bitangent.
        for (Mesh m : glTF.getMeshes()) {
            buildTBN(glTF, m.getPrimitives());
        }
        if (gles != null && com.nucleus.renderer.Configuration.getInstance().isUseVBO()) {
            BufferObjectsFactory.getInstance().createVBOs(gles, glTF.getBuffers(null));
            SimpleLogger.d(getClass(), "Created VBOs for gltf assets");
        }

    }

    public void buildTBN(GLTF gltf, Primitive[] primitives) {
        if (primitives != null) {
            for (Primitive p : primitives) {
                p.calculateTBN(gltf);
            }
        }
    }

    /**
     * Deletes loaded gltf assets. This will delete binary buffers and texture images and then remove
     * the gltf asset from AssetManager.
     * Do not call this wile gltf model is in use - must call outside from render.
     * After this call the gltf asset must be loaded in order to be used again.
     * 
     * @param gles
     * @param gltf
     * @throws GLException
     */
    public void deleteGLTFAssets(GLES20Wrapper gles, GLTF gltf) throws GLException {
        BufferObjectsFactory.getInstance().destroyVBOs(gles, gltf.getBuffers(null));
        deleteTextures(gles, gltf, gltf.getImages());
        gltfAssets.remove(gltf.getFilename());
        gltf.destroy();
    }

    protected void deleteTextures(GLES20Wrapper gles, GLTF gltf, Image[] images) {
        int deleted = 0;
        if (images != null) {
            int[] names = new int[1];
            for (Image image : images) {
                names[0] = image.getTextureName();
                if (names[0] > 0) {
                    gles.glDeleteTextures(names);
                    image.setTextureName(0);
                    deleted++;
                }
                if (image.getBufferImage() != null) {
                    destroyBufferImage(gltf, image);
                }
            }
        }
        SimpleLogger.d(getClass(), "Deleted " + deleted + " textures");
    }

    protected void deleteTextures(GLES20Wrapper gles, Texture2D[] textures) {
        int deleted = 0;
        if (textures != null) {
            int[] names = new int[1];
            for (Texture2D texture : textures) {
                names[0] = texture.getName();
                if (names[0] > 0) {
                    gles.glDeleteTextures(names);
                    texture.setTextureName(0);
                    deleted++;
                }
            }
        }
        SimpleLogger.d(getClass(), "Deleted " + deleted + " textures");
    }

    /**
     * Loads a glTF asset, this will not load binary data (buffers) or texture images.
     * The returned asset is resolved using {@link RuntimeResolver}
     * 
     * @param path Path where gltf assets such as binary buffers and images are loaded from.
     * @param name The filename
     * @param is
     * @return The loaded glTF asset without any buffers or image loaded.
     * @throws IOException
     * @throws GLTFException If there is an error in the glTF or it cannot be resolved
     */
    private GLTF loadJSONAsset(String path, String fileName, InputStream is)
            throws IOException, GLTFException {
        GLTF glTF = null;
        try {
            Reader reader = new InputStreamReader(is, "UTF-8");
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            glTF = gson.fromJson(reader, GLTF.class);
        } catch (UnsupportedEncodingException e) {
            SimpleLogger.d(getClass(), e.getMessage());
            return null;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Could not find gltf asset with name " + fileName);
        }
        glTF.setPath(path);
        glTF.setFilename(fileName);
        glTF.resolve();
        return glTF;
    }

    /**
     * Loads the gltf buffers with binary data.
     * TODO - Handle Buffer URIs so that a binary buffer is only loaded once even if it is referenced in several
     * Nodes/Assets.
     * 
     * @param glTF
     * @throws IOException
     */
    protected void loadBuffers(GLTF glTF) throws IOException {
        try {
            for (Buffer b : glTF.getBuffers(null)) {
                b.createBuffer();
                b.load(glTF, b.getUri());
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    /**
     * Loads all textures for the specified material
     * 
     * @param gles
     * @param gltf
     * @param materials
     * @throws IOException
     */
    protected void loadTextures(GLES20Wrapper gles, GLTF gltf, Material[] materials) throws IOException {
        if (materials != null) {
            for (Material material : materials) {
                PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
                loadTextures(gles, gltf, pbr);
                loadTexture(gles, gltf, material.getNormalTexture(), ColorModel.LINEAR);
            }
        }
    }

    /**
     * Loads the textures needed for the PBR material property, if texture bufferimage already loaded
     * for a texture then it is skipped.
     * 
     * @param gles
     * @param gltf
     * @param pbr
     * @throws IOException
     */
    protected void loadTextures(GLES20Wrapper gles, GLTF gltf, PBRMetallicRoughness pbr) throws IOException {
        loadTexture(gles, gltf, pbr.getBaseColorTexture(), ColorModel.SRGB);
    }

    /**
     * Loads the texture - if bufferimage is already present for the texture then nothing is done.
     * If texInfo is null then nothing is done
     * 
     * @param gles
     * @param gltf
     * @param texInfo
     * @param colorMode If model is linear or srgb
     * @throws IOException
     */
    protected void loadTexture(GLES20Wrapper gles, GLTF gltf, TextureInfo texInfo, BufferImage.ColorModel colorModel)
            throws IOException {
        if (texInfo != null && gltf.getTexture(texInfo).getImage().getBufferImage() == null) {
            // Have not loaded bufferimage for this texture
            Texture texture = gltf.getTexture(texInfo);
            Image img = texture.getImage();
            BufferImage bufferImage = getTextureImage(gltf.getPath(img.getUri()));
            bufferImage.setColorModel(colorModel);
            img.setBufferImage(bufferImage);
            internalCreateTexture(gles, img);
        }
    }

    /**
     * Creates and uploads the texture
     * 
     * @param gles
     * @param image
     */
    private void internalCreateTexture(GLES20Wrapper gles, Image image) {
        try {
            int[] name = createTextureName(gles);
            image.setTextureName(name[0]);
            TextureUtils.uploadTextures(gles, image, true);
        } catch (GLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void destroyBufferImage(GLTF gltf, Image image) {
        BufferImage.destroyImages(new BufferImage[] { image.getBufferImage() });
        image.setBufferImage(null);
        images.remove(gltf.getPath(image.getUri()));
    }

    /**
     * Returns the texture image, if not already loaded the image is loaded and returned.
     * 
     * @param uri Full path to Image resource
     * @return
     * @throws IOException
     */
    protected BufferImage getTextureImage(String uri) throws IOException {
        if (uri != null) {
            BufferImage textureImage = images.get(uri);
            if (textureImage == null) {
                textureImage = BaseImageFactory.getInstance().createImage(uri, null);
                images.put(uri, textureImage);
            }
            return textureImage;
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

    /**
     * Creates an empty Texture object from the external reference, this is the main entrypoint for creating
     * Texture object from file (json)
     * Before calling this make sure the reference is not an id reference.
     * Avoid calling this method directly - use {@link #getTexture(GLES20Wrapper, ImageFactory, ExternalReference)}
     * 
     * @param ref Reference to JSON .tex file
     * @return
     * @throws FileNotFoundException
     */
    protected Texture2D createTexture(ExternalReference ref) throws FileNotFoundException {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Texture2D.class, new TextureDeserializer());
        Gson gson = builder.create();
        SimpleLogger.d(TextureFactory.class, "Reading texture data from: " + ref.getSource());
        InputStreamReader reader;
        try {
            InputStream is = ref.getAsStream();
            if (is == null) {
                throw new FileNotFoundException("Could not find file " + ref.getSource());
            }
            // TODO - If Android build version => 19 then java.nio.StandardCharset can be used
            reader = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Texture2D texture = gson.fromJson(reader, Texture2D.class);
        if (texture.getId() == null) {
            throw new IllegalArgumentException("Texture object id is null for ref: " + ref.getSource());
        }
        if (loadedTextures.containsKey(texture.getId())) {
            throw new IllegalArgumentException("Already loaded texture with id: " + texture.getId());
        }
        if (!texture.validateTextureParameters()) {
            throw new IllegalArgumentException(
                    "Texture parameters not valid for:" + ref.getSource() + " : " + texture.getTexParams());
        }
        if (texture.getTextureType() != TextureType.Untextured) {
            // Make sure the loaded texture has an external reference.
            if (texture.getExternalReference() == null) {
                throw new IllegalArgumentException("External reference is null in texture " + ref.getSource());
            }

        }
        return texture;
    }

    /**
     * Creates a texture for the specified image,
     * The texture will be uploaded to GL using a created texture object name, if several mip-map levels are
     * supplied they will be used.
     * If the device current resolution is lower than the texture target resolution then a lower mip-map level is used.
     * 
     * @param gles The gles wrapper
     * @param imageFactory factor for image creation
     * @param source The texture source, the new texture will be a copy of this with the texture image loaded into GL.
     * @return A new texture object containing the texture image.
     */
    protected Texture2D createTexture(GLES20Wrapper gles, ImageFactory imageFactory, Texture2D source) {
        Texture2D texture = TextureFactory.getInstance().createTexture(source);
        internalCreateTexture(gles, imageFactory, texture);
        return texture;
    }

    /**
     * Internal method to create a texture based on the texture setup source, the texture will be set with data from
     * texture setup and uploaded to GL.
     * If the texture source is an external reference the texture image is fetched from {@link AssetManager}
     * If the texture source is a dynamic id reference it is looked for and setup if found.
     * Texture parameters are uploaded.
     * When this method returns the texture is ready to be used.
     * 
     * @param gles
     * @param texture The texture
     * @param imageFactory The imagefactory to use for image creation
     */
    private void internalCreateTexture(GLES20Wrapper gles, ImageFactory imageFactory, Texture2D texture) {
        if (texture.getTextureType() == TextureType.Untextured) {
            return;
        }
        BufferImage[] textureImg = TextureUtils
                .loadTextureMIPMAP(imageFactory, texture);
        internalCreateTexture(gles, textureImg, texture);
    }

    private void internalCreateTexture(GLES20Wrapper gles, BufferImage[] textureImg, Texture2D texture) {
        if (textureImg[0].getResolution() != null) {
            if (texture.getWidth() > 0 || texture.getHeight() > 0) {
                throw new IllegalArgumentException("Size is already set in texture " + texture.getId());
            }
            texture.setResolution(textureImg[0].getResolution());
        }
        try {
            int[] name = createTextureName(gles);
            texture.setTextureName(name[0]);
            TextureUtils.uploadTextures(gles, texture, textureImg);
            SimpleLogger.d(getClass(), "Uploaded texture " + texture.toString());
            BufferImage.destroyImages(textureImg);
        } catch (GLException e) {
            throw new IllegalArgumentException(e);
        }

    }

    /**
     * Creates a new texture, allocating a texture for the specified size.
     * 
     * @param gles
     * @param type
     * @param id
     * @param resolution
     * @param size
     * @param format Image format
     * @param texParams
     * @params target Texture target
     * @return
     */
    protected Texture2D createTexture(GLES20Wrapper gles, TextureType type, String id, RESOLUTION resolution,
            int[] size, ImageFormat format, TextureParameter texParams, int target) throws GLException {
        int[] textureName = createTextureName(gles);
        Texture2D result = TextureFactory.getInstance().createTexture(type, id, resolution, texParams, size, format,
                textureName[0]);
        gles.glBindTexture(target, result.getName());
        gles.texImage(result);
        GLUtils.handleError(gles, "glTexImage2D");
        return result;
    }

    public int[] createTextureName(GLES20Wrapper gles) {
        int[] textures = new int[1];
        gles.glGenTextures(textures);
        return textures;
    }

    /**
     * Utility method to return a list with the folder in the specified resource path
     * 
     * @param path List of subfolders of this path will be returned
     * @return folders in the specified path (excluding the path in the returned folder names)
     */
    public String[] listResourceFolders(String path) {
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource(path);
        if (url == null) {
            return new String[0];
        }
        File[] files = new File(url.getFile()).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        // Truncate name so only include the part after specified path.
        String[] folders = new String[files.length];
        int index = 0;
        path = path.replace('/', File.separatorChar);
        for (File f : files) {
            int start = f.toString().indexOf(path);
            folders[index++] = f.toString().substring(start + path.length());
        }
        return folders;
    }

    /**
     * 
     * @param path
     * @param folders
     * @param mime
     * @return List of folder/filname for files that ends with mime
     */
    public ArrayList<String> listFiles(String path, String[] folders, final String mime) {
        ArrayList<String> result = new ArrayList<>();
        ClassLoader loader = getClass().getClassLoader();
        String comparePath = path.replace('/', File.separatorChar);
        for (String folder : folders) {
            URL url = loader.getResource(path + folder);
            File[] files = new File(url.getFile()).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(mime);
                }
            });
            // Truncate name so only include the part after specified path.
            for (File f : files) {
                int start = f.toString().indexOf(comparePath);
                result.add(f.toString().substring(start + comparePath.length()));
            }
        }
        return result;
    }

}
