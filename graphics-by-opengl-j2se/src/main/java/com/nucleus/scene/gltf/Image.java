package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.texturing.Texture2D;

/**
 * 
 * image
 * Image data used to create a texture. Image can be referenced by URI or bufferView index. mimeType is required in the
 * latter case.
 * Properties
 * 
 * Type Description Required
 * uri string The uri of the image. No
 * mimeType string The image's MIME type. No
 * bufferView integer The index of the bufferView that contains the image. Use this instead of the image's uri property.
 * No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 */

public class Image extends GLTFNamedValue {

    private static final String URI = "uri";
    private static final String MIME_TYPE = "mimeType";
    private static final String BUFFER_VIEW = "bufferView";

    @SerializedName(URI)
    private String uri;
    @SerializedName(MIME_TYPE)
    private String mimeType;
    @SerializedName(BUFFER_VIEW)
    private int bufferView;

    transient private Texture2D texture;

    /**
     * Sets the immutable texture
     * 
     * @param texture
     */
    public void setTextureImage(Texture2D texture) {
        this.texture = texture;
    }

    public Texture2D getTexture() {
        return texture;
    }

    public String getUri() {
        return uri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getBufferView() {
        return bufferView;
    }

}
