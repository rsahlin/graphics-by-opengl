package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;

/**
 * asset
 * Metadata about the glTF asset.
 * 
 * Properties
 * Type Description Required
 * copyright string A copyright message suitable for display to credit the content creator. No
 * generator string Tool that generated this glTF model. Useful for debugging. No
 * version string The glTF version that this asset targets. ✅ Yes
 * minVersion string The minimum glTF version that this asset targets. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 */
public class Asset {

    private static final String COPYRIGHT = "copyright";
    private static final String GENERATOR = "generator";
    private static final String VERSION = "version";
    private static final String MIN_VERSION = "minVersion";

    @SerializedName(COPYRIGHT)
    private String copyright;
    @SerializedName(GENERATOR)
    private String generator;
    @SerializedName(VERSION)
    private String version;
    @SerializedName(MIN_VERSION)
    private String minVersion;

    /**
     * Not supported yet
     */
    // private Object extensions;
    // private Object extras;

    public String getCopyright() {
        return copyright;
    }

    public String getGenerator() {
        return generator;
    }

    public String getVersion() {
        return version;
    }

    public String getMinVersion() {
        return minVersion;
    }

}
