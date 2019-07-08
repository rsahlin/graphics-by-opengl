package com.nucleus.assets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.SimpleLogger;
import com.nucleus.io.ExternalReference;
import com.nucleus.io.gson.TextureDeserializer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Image;
import com.nucleus.shader.Shader;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.BufferImage.ImageFormat;
import com.nucleus.texturing.ImageFactory;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Format;
import com.nucleus.texturing.Texture2D.Type;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureParameter.Parameter;
import com.nucleus.texturing.TextureType;

public abstract class BaseAssets implements Assets {

    protected final static String NULL_PARAMETER = "Parameter is null: ";
    /**
     * Store textures using the source image name.
     */
    protected HashMap<String, Texture2D> textures = new HashMap<>();

    /**
     * Loaded images that are used to create textures - clear when textures are created
     */
    protected HashMap<String, BufferImage> images = new HashMap<>();

    protected HashMap<String, GraphicsPipeline> graphicPipelines = new HashMap<>();

    protected HashMap<String, GLTF> gltfAssets = new HashMap<>();

    /**
     * Keep track of loaded texture objects by id
     */
    protected static final Map<String, Texture2D> loadedTextures = new HashMap<>();

    /**
     * Creates one texture name
     * 
     * @return
     */
    protected abstract int[] createTextureName();

    /**
     * Creates a graphics pipeline from the renderer and shader
     * 
     * @param shader
     * @return The pipeline
     */
    protected abstract GraphicsPipeline createGraphicsPipeline(Shader shader);

    @Override
    public Texture2D getTexture(NucleusRenderer renderer, ImageFactory imageFactory, ExternalReference ref)
            throws IOException {
        if (ref == null) {
            throw new IllegalArgumentException(NULL_PARAMETER);
        }
        String idRef = ref.getIdReference();
        if (idRef != null) {
            return getTexture(idRef);
        } else {
            try {
                return getTexture(renderer, imageFactory, createTexture(ref));
            } catch (BackendException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Texture2D getTexture(NucleusRenderer renderer, ImageFactory imageFactory, String id,
            ExternalReference externalReference, RESOLUTION resolution, TextureParameter parameter, int mipmap) {
        Texture2D source = TextureFactory.getInstance().createTexture(TextureType.Texture2D, id, externalReference,
                resolution, parameter, mipmap, Format.RGBA, Type.UNSIGNED_BYTE);
        try {
            internalCreateTexture(renderer, imageFactory, source);
        } catch (BackendException e) {
            throw new RuntimeException(e);
        }
        return source;
    }

    @Override
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

    @Override
    public Texture2D createTexture(NucleusRenderer renderer, RenderTarget renderTarget, AttachementData attachement)
            throws BackendException {
        if (renderTarget.getId() == null) {
            throw new IllegalArgumentException("RenderTarget must have an id");
        }
        Texture2D texture = textures.get(renderTarget.getAttachementId(attachement));
        if (texture == null) {
            try {
                // TODO - What values should be used when creating the texture?
                TextureType type = TextureType.DynamicTexture2D;
                RESOLUTION resolution = RESOLUTION.HD;
                int[] size = attachement.getSize();
                TextureParameter texParams = new TextureParameter(
                        new Parameter[] { Parameter.NEAREST, Parameter.NEAREST, Parameter.CLAMP,
                                Parameter.CLAMP });
                ImageFormat format = attachement.getFormat();
                texture = createTexture(renderer, type, renderTarget.getId(), resolution, size, format,
                        texParams, GLES20.GL_TEXTURE_2D);
                texture.setId(renderTarget.getAttachementId(attachement));
                textures.put(renderTarget.getAttachementId(attachement), texture);
                SimpleLogger.d(getClass(), "Created texture: " + texture.toString());
            } catch (GLException e) {
                throw new BackendException(e.getMessage());
            }
        }
        return texture;
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
     * Returns the texture, if the texture has not been loaded it will be loaded and stored in the assetmanager.
     * If already has been loaded the loaded instance will be returned.
     * Treat textures as immutable object
     * 
     * @param renderer
     * @param imageFactory
     * @param source The external ref is used to load a texture
     * @return The texture specifying the external reference to the texture to load and return.
     * @throws IOException
     * @throws BackendException
     */
    protected Texture2D getTexture(NucleusRenderer renderer, ImageFactory imageFactory, Texture2D source)
            throws IOException, BackendException {
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
                texture = createTexture(renderer, imageFactory, source);
                textures.put(refSource, texture);
                FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_TEXTURE,
                        " " + texture.getName() + " : " + refSource, start,
                        System.currentTimeMillis());
            }
            return texture;
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
     * Creates a new texture, allocating a texture for the specified size.
     * 
     * @param renderer
     * @param type
     * @param id
     * @param resolution
     * @param size
     * @param format Image format
     * @param texParams
     * @params target Texture target
     * @return
     */
    protected Texture2D createTexture(NucleusRenderer renderer, TextureType type, String id, RESOLUTION resolution,
            int[] size, ImageFormat format, TextureParameter texParams, int target) throws BackendException {
        int[] textureName = createTextureName();
        Texture2D result = TextureFactory.getInstance().createTexture(type, id, resolution, texParams, size, format,
                textureName[0]);
        createTexture(result, target);
        return result;
    }

    /**
     * Creates a texture for the specified image,
     * The texture will be uploaded to GL using a created texture object name, if several mip-map levels are
     * supplied they will be used.
     * If the device current resolution is lower than the texture target resolution then a lower mip-map level is used.
     * 
     * @param renderer
     * @param imageFactory factor for image creation
     * @param source The texture source, the new texture will be a copy of this with the texture image loaded into GL.
     * @return A new texture object containing the texture image.
     * @throws BackendException
     */
    protected Texture2D createTexture(NucleusRenderer renderer, ImageFactory imageFactory, Texture2D source)
            throws BackendException {
        Texture2D texture = TextureFactory.getInstance().createTexture(source);
        internalCreateTexture(renderer, imageFactory, texture);
        return texture;
    }

    /**
     * Creates and uploads the texture
     * 
     * @param renderer
     * @param image
     * @throws BackendException
     */
    protected void internalCreateTexture(NucleusRenderer renderer, Image image) throws BackendException {
        int[] name = createTextureName();
        image.setTextureName(name[0]);
        renderer.uploadTextures(image, true);
    }

    /**
     * Internal method to create a texture based on the texture setup source, the texture will be set with data from
     * texture setup and uploaded to GL.
     * If the texture source is an external reference the texture image is fetched from {@link Assets}
     * If the texture source is a dynamic id reference it is looked for and setup if found.
     * Texture parameters are uploaded.
     * When this method returns the texture is ready to be used.
     * 
     * @param renderer
     * @param imageFactory The imagefactory to use for image creation
     * @param texture The texture
     * @throws BackendException
     */
    protected void internalCreateTexture(NucleusRenderer renderer, ImageFactory imageFactory, Texture2D texture)
            throws BackendException {
        if (texture.getTextureType() == TextureType.Untextured) {
            return;
        }
        BufferImage[] textureImg = loadTextureMIPMAP(imageFactory, texture);
        internalCreateTexture(renderer, textureImg, texture);
    }

    protected void internalCreateTexture(NucleusRenderer renderer, BufferImage[] textureImg, Texture2D texture)
            throws BackendException {
        if (textureImg[0].getResolution() != null) {
            if (texture.getWidth() > 0 || texture.getHeight() > 0) {
                throw new IllegalArgumentException("Size is already set in texture " + texture.getId());
            }
            texture.setResolution(textureImg[0].getResolution());
        }
        int[] name = createTextureName();
        texture.setTextureName(name[0]);
        renderer.uploadTextures(texture, textureImg);
        SimpleLogger.d(getClass(), "Uploaded texture " + texture.toString());
        BufferImage.destroyImages(textureImg);
    }

    @Override
    public GraphicsPipeline getGraphicsPipeline(NucleusRenderer renderer, Shader shader) {
        String key = shader.getKey();
        GraphicsPipeline compiled = graphicPipelines.get(key);
        if (compiled != null) {
            return compiled;
        }
        try {
            long start = System.currentTimeMillis();
            shader.createProgram(renderer);
            FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_SHADER, shader.getClass().getSimpleName(),
                    start,
                    System.currentTimeMillis());
            compiled = createGraphicsPipeline(shader);
            graphicPipelines.put(key, compiled);
            SimpleLogger.d(getClass(), "Stored graphics pipeline with key: " + key);
            return compiled;
        } catch (BackendException e) {
            throw new RuntimeException(e);
        }
    }

}
