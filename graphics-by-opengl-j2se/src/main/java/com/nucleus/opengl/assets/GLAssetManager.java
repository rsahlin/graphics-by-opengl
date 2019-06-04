package com.nucleus.opengl.assets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.SimpleLogger;
import com.nucleus.assets.Assets;
import com.nucleus.io.ExternalReference;
import com.nucleus.io.gson.TextureDeserializer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLPipeline;
import com.nucleus.opengl.GLUtils;
import com.nucleus.opengl.TextureUtils;
import com.nucleus.opengl.shader.GLShaderProgram;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.renderer.Window;
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
import com.nucleus.scene.gltf.Texture.Swizzle.Component;
import com.nucleus.scene.gltf.Texture.TextureInfo;
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
public class GLAssetManager implements Assets {
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

    private HashMap<String, GraphicsPipeline> graphicPipelines = new HashMap<>();

    private HashMap<String, GLTF> gltfAssets = new HashMap<>();

    /**
     * Keep track of loaded texture objects by id
     */
    private static final Map<String, Texture2D> loadedTextures = new HashMap<>();

    protected GLES20Wrapper gles;

    /**
     * Internal constructor - do not use directly
     */
    public GLAssetManager(GLES20Wrapper gles) {
        this.gles = gles;
    }

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

    @Override
    public GraphicsPipeline getPipeline(NucleusRenderer renderer, GLShaderProgram program) {
        String key = program.getKey();
        GraphicsPipeline compiled = graphicPipelines.get(key);
        if (compiled != null) {
            return compiled;
        }
        try {
            long start = System.currentTimeMillis();
            program.createProgram(renderer);
            FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_SHADER, program.getClass().getSimpleName(),
                    start,
                    System.currentTimeMillis());
            compiled = new GLPipeline(renderer, program);
            graphicPipelines.put(key, compiled);
            SimpleLogger.d(getClass(), "Stored graphics pipeline with key: " + key);
            return compiled;
        } catch (BackendException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes all references and resources.
     * 
     * @param renderer
     */
    @Override
    public void destroy(NucleusRenderer renderer) {
        SimpleLogger.d(getClass(), "destroy");
        deletePrograms(renderer);
        deleteTextures(renderer);
    }

    private void deleteTextures(NucleusRenderer renderer) {
        if (textures.size() == 0) {
            return;
        }
        Texture2D[] t = new Texture2D[textures.size()];
        int i = 0;
        for (Texture2D texture : textures.values()) {
            t[i++] = texture;
        }
        deleteTextures(t);
        textures.clear();
    }

    private void deletePrograms(NucleusRenderer renderer) {
        for (GraphicsPipeline pipeline : graphicPipelines.values()) {
            renderer.deletePipeline(pipeline);
        }
        graphicPipelines.clear();
    }

    @Override
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

    @Override
    public void loadGLTFAssets(NucleusRenderer renderer, GLTF glTF) throws IOException, BackendException {
        loadBuffers(glTF);
        loadTextures(renderer, glTF, glTF.getMaterials());
        SimpleLogger.d(getClass(), "Loaded gltf assets");
        // Build TBN before creating VBOs
        // This can mean that a number of buffers needs to be created, for instance normal, tangent and bitangent.
        long start = System.currentTimeMillis();
        for (Mesh m : glTF.getMeshes()) {
            buildTBN(glTF, m.getPrimitives());
        }
        FrameSampler.getInstance().logTag(FrameSampler.Samples.PROCESS_BUFFERS, "_TBN", start,
                System.currentTimeMillis());
        if (com.nucleus.renderer.Configuration.getInstance().isUseVBO()) {
            try {
                renderer.getBufferFactory().createVBOs(glTF.getBuffers(null));
                SimpleLogger.d(getClass(), "Created VBOs for gltf assets");
            } catch (GLException e) {
                throw new BackendException(e.getMessage());
            }
        }

    }

    protected void buildTBN(GLTF gltf, Primitive[] primitives) {
        if (primitives != null) {
            for (Primitive p : primitives) {
                p.calculateTBN(gltf);
            }
        }

    }

    @Override
    public void deleteGLTFAssets(NucleusRenderer renderer, GLTF gltf) throws BackendException {
        try {
            renderer.getBufferFactory().destroyVBOs(renderer, gltf.getBuffers(null));
            deleteTextures(renderer, gltf, gltf.getImages());
            gltfAssets.remove(gltf.getFilename());
            gltf.destroy();
        } catch (GLException e) {
            throw new BackendException(e.getMessage());
        }
    }

    protected void deleteTextures(NucleusRenderer renderer, GLTF gltf, Image[] images) {
        int deleted = 0;
        if (images != null) {
            for (Image image : images) {
                deleteTexture(image);
                image.setTextureName(0);
                deleted++;
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
        long start = System.currentTimeMillis();
        try {
            for (Buffer b : glTF.getBuffers(null)) {
                b.createBuffer();
                b.load(glTF, b.getUri());
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        FrameSampler.getInstance().logTag(FrameSampler.Samples.LOAD_BUFFERS, "_GLTF", start,
                System.currentTimeMillis());
    }

    /**
     * Loads all textures for the specified materials
     * 
     * @param renderer
     * @param gltf
     * @param materials
     * @throws IOException
     * @throws BackendException
     */
    protected void loadTextures(NucleusRenderer renderer, GLTF gltf, Material[] materials)
            throws IOException, BackendException {
        long start = System.currentTimeMillis();
        if (materials != null) {
            for (Material material : materials) {
                loadTextures(renderer, gltf, material);
            }
        }
        FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_TEXTURE, "_ALL", start,
                System.currentTimeMillis());
    }

    /**
     * Loads the textures needed for the PBR and material property, if texture bufferimage already loaded
     * for a texture then it is skipped.
     * 
     * @param renderer
     * @param gltf
     * @param material
     * @throws IOException
     */
    protected void loadTextures(NucleusRenderer renderer, GLTF gltf, Material material)
            throws IOException, BackendException {
        PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
        loadTexture(renderer, gltf, pbr.getBaseColorTexture(), null, ColorModel.SRGB);
        TextureInfo mrInfo = pbr.getMetallicRoughnessTexture();
        loadTexture(renderer, gltf, material.getNormalTexture(), ImageFormat.RGB, ColorModel.LINEAR);
        TextureInfo occlInfo = material.getOcclusionTexture();
        if (mrInfo != null && occlInfo != null && mrInfo.getIndex() == occlInfo.getIndex()) {
            // Material has both metallicroughness and occlusion in the same texture
            loadTexture(renderer, gltf, mrInfo, ImageFormat.RGB, ColorModel.LINEAR);
        } else {
            Texture mr = loadTexture(renderer, gltf, mrInfo, ImageFormat.RG,
                    ColorModel.LINEAR);
            if (mr != null) {
                // Need to set texture swizzle so that RG is mapped to GB
                mr.setSwizzle(Component.RED, Component.RED, Component.GREEN, Component.ALPHA);
                loadTexture(renderer, gltf, mrInfo, ImageFormat.RG, ColorModel.LINEAR);
            } else if (occlInfo != null) {
                loadTexture(renderer, gltf, occlInfo, ImageFormat.R, ColorModel.LINEAR);
            }
        }
    }

    /**
     * Loads the texture - if bufferimage is already present for the texture then nothing is done.
     * If texInfo is null then nothing is done
     * 
     * @param renderer
     * @param gltf
     * @param texInfo
     * @param destFormat Optional destination image format, if null then same as source
     * @param colorMode If model is linear or srgb
     * @return The loaded texture object
     * @throws IOException
     */
    protected Texture loadTexture(NucleusRenderer renderer, GLTF gltf, TextureInfo texInfo, ImageFormat destFormat,
            BufferImage.ColorModel colorModel)
            throws IOException, BackendException {
        if (texInfo != null && gltf.getTexture(texInfo).getImage().getBufferImage() == null) {
            // Have not loaded bufferimage for this texture
            long start = System.currentTimeMillis();
            Texture texture = gltf.getTexture(texInfo);
            Image img = texture.getImage();
            BufferImage bufferImage = getTextureImage(gltf.getPath(img.getUri()), destFormat);
            bufferImage.setColorModel(colorModel);
            img.setBufferImage(bufferImage);
            internalCreateTexture(renderer, img);
            FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_TEXTURE, " " + texture.getName(), start,
                    System.currentTimeMillis());
            return texture;
        }
        return null;
    }

    @Override
    public BufferImage[] loadTextureMIPMAP(ImageFactory imageFactory, Texture2D texture) {
        try {
            long start = System.currentTimeMillis();
            BufferImage image = loadTextureImage(imageFactory, texture);
            long loaded = System.currentTimeMillis();
            FrameSampler.getInstance()
                    .logTag(FrameSampler.Samples.CREATE_IMAGE, " " + texture.getExternalReference().getSource(), start,
                            loaded);
            int levels = texture.getLevels();
            if (levels == 0 || !texture.getTexParams().isMipMapFilter()) {
                levels = 1;
            }
            // Do not use levels, mipmaps created when textures are uploaded
            levels = 1;
            BufferImage[] images = new BufferImage[levels];
            images[0] = image;
            return images;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the texture image for the texture, fitting the result for the texture resolution bias and screen resolution
     * 
     * @param factory
     * @param texture
     * @return
     * @throws IOException
     */
    protected BufferImage loadTextureImage(ImageFactory factory, Texture2D texture) throws IOException {
        SimpleLogger.d(TextureUtils.class, "Loading image " + texture.getExternalReference().getSource());
        float scale = (float) Window.getInstance().getHeight() / texture.getResolution().lines;
        if (scale < 0.9) {
            RESOLUTION res = RESOLUTION.getResolution(Window.getInstance().getHeight());
            BufferImage img = factory.createImage(texture.getExternalReference().getSource(), scale, scale,
                    TextureUtils.getImageFormat(texture), res);
            SimpleLogger.d(TextureUtils.class,
                    "Image scaled " + scale + " to " + img.getWidth() + ", " + img.getHeight()
                            + " for target resolution " + res);
            return img;
        }
        return factory.createImage(texture.getExternalReference().getSource(), TextureUtils.getImageFormat(texture));

    }

    /**
     * Creates and uploads the texture
     * 
     * @param renderer
     * @param image
     * @throws BackendException
     */
    private void internalCreateTexture(NucleusRenderer renderer, Image image) throws BackendException {
        int[] name = renderer.createTextureName();
        image.setTextureName(name[0]);
        renderer.uploadTextures(image, true);
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
     * @param destFormat Optional destination image format, if null then same format as source will be chosen.
     * @return
     * @throws IOException
     */
    protected BufferImage getTextureImage(String uri, ImageFormat destFormat) throws IOException {
        if (uri != null) {
            BufferImage textureImage = images.get(uri);
            if (textureImage == null) {
                textureImage = BaseImageFactory.getInstance().createImage(uri, destFormat);
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
     * Internal method to create a texture based on the texture setup source, the texture will be set with data from
     * texture setup and uploaded to GL.
     * If the texture source is an external reference the texture image is fetched from {@link Assets}
     * If the texture source is a dynamic id reference it is looked for and setup if found.
     * Texture parameters are uploaded.
     * When this method returns the texture is ready to be used.
     * 
     * @param renderer
     * @param texture The texture
     * @param imageFactory The imagefactory to use for image creation
     * @throws BackendException
     */
    private void internalCreateTexture(NucleusRenderer renderer, ImageFactory imageFactory, Texture2D texture)
            throws BackendException {
        if (texture.getTextureType() == TextureType.Untextured) {
            return;
        }
        BufferImage[] textureImg = loadTextureMIPMAP(imageFactory, texture);
        internalCreateTexture(renderer, textureImg, texture);
    }

    private void internalCreateTexture(NucleusRenderer renderer, BufferImage[] textureImg, Texture2D texture)
            throws BackendException {
        if (textureImg[0].getResolution() != null) {
            if (texture.getWidth() > 0 || texture.getHeight() > 0) {
                throw new IllegalArgumentException("Size is already set in texture " + texture.getId());
            }
            texture.setResolution(textureImg[0].getResolution());
        }
        int[] name = renderer.createTextureName();
        texture.setTextureName(name[0]);
        renderer.uploadTextures(texture, textureImg);
        SimpleLogger.d(getClass(), "Uploaded texture " + texture.toString());
        BufferImage.destroyImages(textureImg);
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
        int[] textureName = renderer.createTextureName();
        Texture2D result = TextureFactory.getInstance().createTexture(type, id, resolution, texParams, size, format,
                textureName[0]);
        createTexture(result, target);
        return result;
    }

    @Override
    public void createTexture(Texture2D texture, int target) throws BackendException {
        gles.glBindTexture(target, texture.getName());
        gles.texImage(texture);
        GLUtils.handleError(gles, "glTexImage2D");
    }

    @Override
    public void deleteTextures(Texture2D[] textures) {
        int[] names = new int[textures.length];
        for (int i = 0; i < textures.length; i++) {
            names[i] = textures[i].getName();
        }
        gles.glDeleteTextures(names);
    }

    @Override
    public void deleteTexture(Image image) {
        gles.glDeleteTextures(new int[] { image.getTextureName() });
    }

}
