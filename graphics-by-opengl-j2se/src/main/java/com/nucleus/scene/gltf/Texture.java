package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.scene.gltf.Texture.Swizzle.Component;

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

    public static class Swizzle {

        public enum Component {
            RED(GLES30.GL_RED),
            GREEN(GLES30.GL_GREEN),
            BLUE(GLES30.GL_BLUE),
            ALPHA(GLES30.GL_ALPHA);

            public final int value;

            Component(int value) {
                this.value = value;
            }

        }

        public final Component swizzleRed;
        public final Component swizzleGreen;
        public final Component swizzleBlue;
        public final Component swizzleAlpha;

        private Swizzle() {
            swizzleRed = Component.RED;
            swizzleGreen = Component.GREEN;
            swizzleBlue = Component.BLUE;
            swizzleAlpha = Component.ALPHA;
        }

        private Swizzle(Component r, Component g, Component b, Component a) {
            swizzleRed = r;
            swizzleGreen = g;
            swizzleBlue = b;
            swizzleAlpha = a;
        }

    }

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
     * normalTextureInfo - Reference to a texture.
     * Type - Description - Required:
     * 
     * index integer The index of the texture. Yes
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

    /**
     * occlusionTextureInfo - Reference to a texture.
     * Type - Description - Required:
     * 
     * index integer The index of the texture. Yes
     * texCoord integer The set index of texture's TEXCOORD attribute used for texture coordinate mapping. No, default:
     * 0
     * strength number A scalar multiplier controlling the amount of occlusion applied. No, default: 1
     * extensions object Dictionary object with extension-specific objects. No
     * extras any Application-specific data. No
     *
     */
    public static class OcclusionTextureInfo extends TextureInfo {
        private static final String STRENGTH = "strength";
        @SerializedName(STRENGTH)
        private float strength = 1;

        /**
         * Returns the scalar strength to be applied to occlusion:
         * A scalar multiplier controlling the amount of occlusion applied.
         * A value of 0.0 means no occlusion. A value of 1.0 means full occlusion.
         * This value affects the resulting color using the formula:
         * occludedColor = lerp(color, color * <sampled occlusion texture value>, <occlusion strength>).
         * This value is ignored if the corresponding texture is not specified. This value is linear.
         * 
         * @return Scalar strength
         */
        public float getStrength() {
            return strength;
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
    transient private Swizzle swizzle = new Swizzle();

    /**
     * Sets the swizzle for the r,g,b and alpha. This sets the source for respective output channel.
     * 
     * @param r The source of the red channel, RED for red
     * @param g The source of the green channel, GREEN for green
     * @param b The source of the blue channel, BLUE for blue
     * @param a The source of the alpha channel, ALPHA for alpha
     */
    public void setSwizzle(Component r, Component g, Component b, Component a) {
        swizzle = new Swizzle(r, g, b, a);
    }

    /**
     * Returns the texture swizzle pattern
     * 
     * @return
     */
    public Swizzle getSwizzle() {
        return swizzle;
    }

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
