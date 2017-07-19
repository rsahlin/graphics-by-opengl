package com.nucleus.texturing;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.SimpleLogger;
import com.nucleus.io.ExternalReference;
import com.nucleus.io.gson.TextureDeserializer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.texturing.Texture2D.Format;
import com.nucleus.texturing.Texture2D.Type;

/**
 * Used to create texture objects.
 * Texture objects created using the same image (external reference) will share texture data, through the texture object
 * id.
 * 
 * @author Richard Sahlin
 *
 */
public class TextureFactory {

    /**
     * Creates a texture for the specified image,
     * The texture will be uploaded to GL using the specified texture object name, if several mip-map levels are
     * supplied they will be used.
     * If the device current resolution is lower than the texture target resolution then a lower mip-map level is used.
     * 
     * @param gles The gles wrapper
     * @param imageFactory factor for image creation
     * @param source The texture source, the new texture will be a copy of this with the texture image loaded into GL.
     * @return A new texture object containing the texture image.
     */
    public static Texture2D createTexture(GLES20Wrapper gles, ImageFactory imageFactory, Texture2D source) {
        Texture2D texture = createTexture(source);
        prepareTexture(gles, imageFactory, texture);
        return texture;
    }

    /**
     * Creates a texture for the specified image, Format will be RGBA and type UNSIGNED_BYTE
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
    public static Texture2D createTexture(GLES20Wrapper gles, ImageFactory imageFactory, String id,
            ExternalReference externalReference, RESOLUTION resolution, TextureParameter parameter, int mipmap) {
        Texture2D source = new Texture2D(id, externalReference, resolution, parameter, mipmap, Format.RGBA,
                Type.UNSIGNED_BYTE);
        return createTexture(gles, imageFactory, source);
    }

    /**
     * Creates a copy of the texture contents (image), this will copy all texture info and the external reference.
     * This will not make a copy of pixel values since these are held in the external reference.
     * 
     * @param source The source texture to copy texture info from.
     * @return Texture object with same contents as the source
     */
    public static Texture2D createTexture(Texture2D source) {
        switch (source.textureType) {
        case Texture2D:
            return new Texture2D(source);
        case TiledTexture2D:
            return new TiledTexture2D((TiledTexture2D) source);
        case UVTexture2D:
            return new UVTexture2D((UVTexture2D) source);
        case Untextured:
            return new Untextured((Untextured) source);
        default:
            throw new IllegalArgumentException();
        }
    }

    public static Texture2D createTexture(ExternalReference ref) throws FileNotFoundException {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Texture2D.class, new TextureDeserializer());
        Gson gson = builder.create();
        try {
            InputStreamReader reader = new InputStreamReader(ref.getAsStream());
            return gson.fromJson(reader, Texture2D.class);
        } catch (Exception e) {
            throw new RuntimeException("Exception reading " + ref.getSource() + " : " + e, e);
        }
    }

    /**
     * Creates a new empty texture object of the specified type
     * 
     * @param type The texture type
     * @return The texture
     */
    public static Texture2D createTexture(TextureType type) {
        switch (type) {
        case Texture2D:
            return new Texture2D();
        case TiledTexture2D:
            return new TiledTexture2D();
        case UVTexture2D:
            return new UVTexture2D();
        case Untextured:
            return new Untextured();
        default:
            throw new IllegalArgumentException("Not implemented support for " + type);
        }
    }

    /**
     * Creates a new TiledTexture
     * 
     * @param id The name of the texture object
     * @param externalReference External reference to the texture image
     * @param targetResolution
     * @param params
     * @param mipmap Number of mipmap levels
     * @param size Width and height of texture atlas, in number of tiles in x and y
     * @param format
     * @param type
     * @return
     */
    public static TiledTexture2D createTiledTexture(String id, ExternalReference externalReference,
            RESOLUTION targetResolution, TextureParameter params, int mipmap, int[] size, Format format, Type type) {
        TiledTexture2D tex = new TiledTexture2D(id, externalReference, targetResolution, params, mipmap, size, format,
                type);
        return tex;
    }

    /**
     * Internal method to create a texture based on the texture setup source, the texture will be set with data from
     * texture setup and uploaded to GL.
     * 
     * @param gles
     * @param texture The texture
     * @param imageFactory The imagefactory to use for image creation
     */
    private static void prepareTexture(GLES20Wrapper gles, ImageFactory imageFactory, Texture2D texture) {
        if (texture.getTextureType() == TextureType.Untextured) {
            return;
        }
        int[] textures = new int[1];
        gles.glGenTextures(1, textures, 0);

        int textureID = textures[0];
        Image[] textureImg = TextureUtils
                .loadTextureMIPMAP(imageFactory, texture);
        try {
            TextureUtils.uploadTextures(gles, GLES20.GL_TEXTURE0, texture, textureID, textureImg);
            // TODO Do not need to keep images after texture is uploaded
            texture.setup(textureID, textureImg);
            SimpleLogger.d(TextureFactory.class, "Uploaded texture " + texture.toString());
        } catch (GLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Copies the texture data, ie the texture name and image data to the destination.
     * 
     * @param source
     * @param destination
     */
    public static void copyTextureData(Texture2D source, Texture2D destination) {
        destination.setup(source.getName(), source.images);
    }

}
