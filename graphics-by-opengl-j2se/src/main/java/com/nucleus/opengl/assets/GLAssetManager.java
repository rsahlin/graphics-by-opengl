package com.nucleus.opengl.assets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.SimpleLogger;
import com.nucleus.assets.BaseAssets;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLPipeline;
import com.nucleus.opengl.GLUtils;
import com.nucleus.opengl.TextureUtils;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
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
import com.nucleus.shader.Shader;
import com.nucleus.texturing.BaseImageFactory;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.BufferImage.ColorModel;
import com.nucleus.texturing.BufferImage.ImageFormat;
import com.nucleus.texturing.ImageFactory;
import com.nucleus.texturing.Texture2D;

/**
 * Implementation of Assets interface for OpenGL/ES - fetch from {@link NucleusRenderer#getAssets()}
 * Loading and unloading assets, mainly textures - this is the main entrypoint for loading of textures
 * and programs.
 * Clients shall only use this class - do not call methods to load assets (program/texture etc) separately.
 *
 */
public class GLAssetManager extends BaseAssets {

    protected GLES20Wrapper gles;

    /**
     * Internal constructor - do not use directly
     * Fetch implementation by calling {@link NucleusRenderer#getAssets()}
     */
    public GLAssetManager(GLES20Wrapper gles) {
        this.gles = gles;
    }

    @Override
    public GraphicsPipeline getPipeline(NucleusRenderer renderer, Shader shader) {
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
            compiled = new GLPipeline(gles, shader);
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

    @Override
    protected int[] createTextureName() {
        int[] textureName = new int[1];
        gles.glGenTextures(textureName);
        return textureName;
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
