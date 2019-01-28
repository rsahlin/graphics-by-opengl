package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;

/**
 * 
 * A texture and its sampler.
 * 
 * Related WebGL functions: createTexture(), deleteTexture(), bindTexture(), texImage2D(), and texParameterf()
 * 
 * Properties
 * 
 * Type Description Required
 * sampler integer The index of the sampler used by this texture. When undefined, a sampler with repeat wrapping and
 * auto filtering should be used. No
 * source integer The index of the image used by this texture. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 */
public class Texture extends GLTFNamedValue implements RuntimeResolver {

    /**
     * textureInfo
     * Reference to a texture.
     *
     * Properties
     * 
     * Type Description Required
     * index integer The index of the texture. ✅ Yes
     * texCoord integer The set index of texture's TEXCOORD attribute used for texture coordinate mapping. No, default:
     * 0
     * extensions object Dictionary object with extension-specific objects. No
     * extras any Application-specific data. No
     *
     */
    public static class TextureInfo {
        private static final String INDEX = "index";
        private static final String TEX_COORD = "texCoord";

        @SerializedName(INDEX)
        private int index;
        @SerializedName(TEX_COORD)
        private int texCoord = 0;

        /**
         * Returns the index of the texture (in the gltf texture array)
         * 
         * @return The gltf index of the texture
         */
        public int getIndex() {
            return index;
        }

        /**
         * This integer value is used to construct a string in the format TEXCOORD_<set index> which is a reference to a
         * key in mesh.primitives.attributes (e.g. A value of 0 corresponds to TEXCOORD_0). Mesh must have corresponding
         * texture coordinate attributes for the material to be applicable to it.
         * 
         * @return The index of the attribute (TEXCOORD_XX) that define the texture coordinates for this object
         */
        public int getTexCoord() {
            return texCoord;
        }

    }

    /**
     * normalTextureInfo
     * Reference to a texture.
     * Properties
     * Type Description Required
     * index integer The index of the texture. ✅ Yes
     * texCoord integer The set index of texture's TEXCOORD attribute used for texture coordinate mapping. No, default:
     * 0
     * scale number The scalar multiplier applied to each normal vector of the normal texture. No, default: 1
     * extensions object Dictionary object with extension-specific objects. No
     * extras any Application-specific data. No *
     */
    public static class NormalTextureInfo extends TextureInfo {
        private static final String SCALE = "scale";

        @SerializedName(SCALE)
        private float scale = 1;

        /**
         * Returns the scalar multiplier applied to each normal vector of the normal texture.
         * 
         * @return Scalar multiplier
         */
        public float getScale() {
            return scale;
        }
    }

    private static final String SAMPLER = "sampler";
    private static final String SOURCE = "source";

    @SerializedName(SAMPLER)
    private int sampler = -1;
    @SerializedName(SOURCE)
    private int source = -1;

    transient private Sampler samplerRef;
    transient private Image imageRef;

    /**
     * Returns the sampler used by this texture
     * 
     * @return
     */
    public Sampler getSampler() {
        return samplerRef;
    }

    public int getSamplerIndex() {
        return sampler;
    }

    public int getSourceIndex() {
        return source;
    }

    /**
     * Returns the image used by this texture, or null if not defined.
     * 
     * @return
     */
    public Image getImage() {
        return imageRef;
    }

    @Override
    public void resolve(GLTF asset) throws GLTFException {
        if (source >= 0) {
            imageRef = asset.getImages()[source];
        }
        if (sampler >= 0) {
            samplerRef = asset.getSamplers()[sampler];
        } else {
            samplerRef = new Sampler();

        }
    }

}
