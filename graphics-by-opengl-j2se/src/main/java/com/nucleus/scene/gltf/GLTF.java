package com.nucleus.scene.gltf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * glTF
 * The root object for a glTF asset.
 * 
 * Properties
 * 
 * Type Description Required
 * extensionsUsed string [1-*] Names of glTF extensions used somewhere in this asset. No
 * extensionsRequired string [1-*] Names of glTF extensions required to properly load this asset. No
 * accessors accessor [1-*] An array of accessors. No
 * animations animation [1-*] An array of keyframe animations. No
 * asset object Metadata about the glTF asset. Yes
 * buffers buffer [1-*] An array of buffers. No
 * bufferViews bufferView [1-*] An array of bufferViews. No
 * cameras camera [1-*] An array of cameras. No
 * images image [1-*] An array of images. No
 * materials material [1-*] An array of materials. No
 * meshes mesh [1-*] An array of meshes. No
 * nodes node [1-*] An array of nodes. No
 * samplers sampler [1-*] An array of samplers. No
 * scene integer The index of the default scene. No
 * scenes scene [1-*] An array of scenes. No
 * skins skin [1-*] An array of skins. No
 * textures texture [1-*] An array of textures. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data.
 *
 */

public class GLTF {

    public static class GLTFException extends Throwable {
        public GLTFException(String reason) {
            super(reason);
        }
    }

    /**
     * For classes in the glTF asset.
     * To make it more convenient to use classes as is without converting to run-time instances, implementations
     * shall look up glTF references through array indexes and replace with object refs.
     * For instance in Accessor the bufferView index shall be used to store a ref to the BufferView that is
     * used by the Accessor.
     *
     */
    public static interface RuntimeResolver {
        /**
         * Resolves the runtime dependencies for glTF asset classes so that they can be used without reference to
         * glTF asset.
         * This must NOT do any other processing apart from putting an object ref instead of index ref, so
         * that the object can be used without reference to glTF asset.
         * 
         * @param asset
         * @throws GLTFException If there is an error resolving or the class has already been resolved.
         */
        public void resolve(GLTF asset) throws GLTFException;

    }

    public Buffer[] getBuffers() {
        return buffers;
    }

    public BufferView[] getBufferViews() {
        return bufferViews;
    }

    public Material[] getMaterials() {
        return materials;
    }

    private static final String ACCESSORS = "accessors";
    private static final String ASSET = "asset";
    private static final String BUFFERS = "buffers";
    private static final String MATERIALS = "materials";
    private static final String SCENES = "scenes";
    private static final String NODES = "nodes";
    private static final String MESHES = "meshes";
    private static final String BUFFER_VIEWS = "bufferViews";
    private static final String SCENE = "scene";

    @SerializedName(ACCESSORS)
    private Accessor[] accessors;
    @SerializedName(ASSET)
    private Asset asset;
    @SerializedName(BUFFERS)
    private Buffer[] buffers;
    @SerializedName(BUFFER_VIEWS)
    private BufferView[] bufferViews;
    @SerializedName(MATERIALS)
    private Material[] materials;
    @SerializedName(MESHES)
    private Mesh[] meshes;
    @SerializedName(NODES)
    private Node[] nodes;

    @SerializedName(SCENE)
    private int scene = -1;
    @SerializedName(SCENES)
    private Scene[] scenes;

    /**
     * From where the main file was loaded - this is needed for loading assets
     */
    transient private String path;

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the path where the resources are loaded from.
     * 
     * @return
     */
    public String getPath() {
        return path;
    }

    public Asset getAsset() {
        return asset;
    }

    public Scene[] getScenes() {
        return scenes;
    }

    /**
     * Returns the value of the default scene, or -1 if not defined.
     * 
     * @return
     */
    public int getScene() {
        return scene;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public Mesh[] getMeshes() {
        return meshes;
    }

    public Accessor[] getAccessors() {
        return accessors;
    }

    /**
     * Resloves all glTF objects so they can be used without reference to glTF asset
     * Call this method only once, normally done when glTF is loaded using the {@link Loader}
     * 
     * @throws GLTFException
     */
    public void resolve() throws GLTFException {
        List<RuntimeResolver> resolves = getResolves();
        for (RuntimeResolver rr : resolves) {
            rr.resolve(this);
        }
    }

    private List<RuntimeResolver> getResolves() {
        ArrayList<RuntimeResolver> result = new ArrayList<>();
        if (accessors != null) {
            result.addAll(Arrays.asList(accessors));
        }
        return result;

    }

    private void resolveAccessors() {
        if (accessors != null) {
            for (Accessor a : accessors) {

            }
        }
    }

}
