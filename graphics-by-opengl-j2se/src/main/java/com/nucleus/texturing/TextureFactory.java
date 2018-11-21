package com.nucleus.texturing;

import com.nucleus.io.ExternalReference;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Image;
import com.nucleus.scene.gltf.Sampler;
import com.nucleus.scene.gltf.Texture;
import com.nucleus.texturing.BufferImage.ImageFormat;
import com.nucleus.texturing.Texture2D.Format;
import com.nucleus.texturing.Texture2D.Type;
import com.nucleus.texturing.TextureParameter.Parameter;

/**
 * Used to create texture objects - ie not texture buffer
 * This class shall only handle creation of texture instance objects, not loading of image buffers.
 * 
 * @author Richard Sahlin
 *
 */
public class TextureFactory {

    private static TextureFactory instance = new TextureFactory();

    public static TextureFactory getInstance() {
        return instance;
    }

    /**
     * Creates a copy of the texture contents (image), this will copy all texture info and the external reference.
     * This will not make a copy of pixel values since these are held in the external reference.
     * 
     * @param source The source texture to copy texture info from.
     * @return Texture object with same contents as the source
     */
    public Texture2D createTexture(Texture2D source) {
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

    /**
     * Creates a new empty texture object of the specified type
     * 
     * @param type The texture type
     * @return The texture
     */
    public Texture2D createTexture(TextureType type) {
        switch (type) {
            case Texture2D:
                return new Texture2D();
            case TiledTexture2D:
                return new TiledTexture2D();
            case UVTexture2D:
                return new UVTexture2D();
            case Untextured:
                return new Untextured();
            case DynamicTexture2D:
                return new DynamicTexture2D();
            default:
                throw new IllegalArgumentException("Not implemented support for " + type);
        }
    }

    /**
     * Creates an empty texture object
     * 
     * @param textureType
     * @param id
     * @param externalReference
     * @param resolution
     * @param params
     * @param mipmap
     * @param format
     * @param type
     * @return The create texture
     */
    public Texture2D createTexture(TextureType textureType, String id, ExternalReference externalReference,
            RESOLUTION resolution, TextureParameter params, int mipmap, Format format, Type type) {
        Texture2D texture = createTexture(textureType);
        texture.setId(id);
        texture.setExternalReference(externalReference);
        texture.setup(resolution, params, mipmap, format, type);
        return texture;
    }

    /**
     * Creates an empty texture object and sets the size - use this for textures that shall be allocated
     * with a texture buffer.
     * 
     * @param textureType
     * @param id
     * @param resolution
     * @param params
     * @param size
     * @param format
     * @param int textureName Non zero texture name
     * @return
     * @throws IllegalArgumentException If texture name is <= 0 or parameter is null.
     */
    public Texture2D createTexture(TextureType textureType, String id, RESOLUTION resolution,
            TextureParameter params, int[] size, ImageFormat format, int textureName) {
        if (textureName <= 0 || size == null || textureType == null || resolution == null || params == null
                || format == null) {
            throw new IllegalArgumentException(
                    "Illegal parameter: " + size + ", " + textureName + ", " + textureType + ", "
                            + resolution + ", " + params + ", " + format);
        }
        Texture2D texture = createTexture(textureType);
        texture.setId(id);
        texture.setup(resolution, params, 1, TextureUtils.getFormat(format), TextureUtils.getType(format));
        texture.setup(size[0], size[1]);
        texture.setTextureName(textureName);
        return texture;
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
    public TiledTexture2D createTiledTexture(String id, ExternalReference externalReference,
            RESOLUTION targetResolution, TextureParameter params, int mipmap, int[] size, Format format, Type type) {
        TiledTexture2D tex = new TiledTexture2D(id, externalReference, targetResolution, params, mipmap, size, format,
                type);
        return tex;
    }

    /**
     * Copies the transient values (texture object name, width, height) and the texture format values into the
     * destination.
     * Use this to copy an instance of an existing texture but to a new type or with different texture parameters.
     * 
     * @param source
     * @param destination
     */
    public void copyTextureInstance(Texture2D source, Texture2D destination) {
        destination.copyInstance(source);
    }

    /**
     * Creates a texture2d object from a glTF texture
     * 
     * @param glTFTexture
     * @return
     */
    public Texture2D createTexture(GLTF glTF, Texture glTFTexture) {
        String id = glTFTexture.getName() != null ? glTFTexture.getName()
                : (glTFTexture.getImage().getName() != null ? glTFTexture.getName() : glTFTexture.getImage().getUri());
        Image img = glTFTexture.getImage();
        TextureParameter textureParameter = createTextureParameter(glTFTexture.getSampler());
        // If mipmapfilter then enable auto generation of mipmap levels
        int levels = textureParameter.isMipMapFilter() ? 2 : 1;
        return createTexture(TextureType.Texture2D, id, new ExternalReference(glTF.getPath(img.getUri())),
                RESOLUTION.ULTRA_HD, textureParameter, levels, Format.RGBA, Type.UNSIGNED_BYTE);

    }

    /**
     * Creates a TextureParameter from the Sampler values.
     * 
     * @param sampler
     * @return
     */
    public TextureParameter createTextureParameter(Sampler sampler) {
        return new TextureParameter(Parameter.get(sampler.getMinFilter()),
                Parameter.get(sampler.getMagFilter()), Parameter.get(sampler.getWrapS()),
                Parameter.get(sampler.getWrapT()));
    }

}
