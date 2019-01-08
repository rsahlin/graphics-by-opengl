package com.nucleus.scene.gltf;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.nucleus.scene.gltf.BufferView.Target;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.Loader;

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

    private static final String ACCESSORS = "accessors";
    private static final String ASSET = "asset";
    private static final String BUFFERS = "buffers";
    private static final String IMAGES = "images";
    private static final String MATERIALS = "materials";
    private static final String SCENES = "scenes";
    private static final String NODES = "nodes";
    private static final String MESHES = "meshes";
    private static final String BUFFER_VIEWS = "bufferViews";
    private static final String CAMERAS = "cameras";
    private static final String SCENE = "scene";
    private static final String SAMPLERS = "samplers";
    private static final String TEXTURES = "textures";

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

    @SerializedName(ACCESSORS)
    private ArrayList<Accessor> accessors;
    @SerializedName(ASSET)
    private Asset asset;
    @SerializedName(BUFFERS)
    private ArrayList<Buffer> buffers;
    @SerializedName(BUFFER_VIEWS)
    private BufferView[] bufferViews;
    @SerializedName(CAMERAS)
    private ArrayList<Camera> cameras = new ArrayList<>();
    @SerializedName(IMAGES)
    private Image[] images;
    @SerializedName(MATERIALS)
    private Material[] materials;
    @SerializedName(MESHES)
    private Mesh[] meshes;
    @SerializedName(NODES)
    private Node[] nodes;
    @SerializedName(SAMPLERS)
    private Sampler[] samplers;
    @SerializedName(TEXTURES)
    private Texture[] textures;

    @SerializedName(SCENE)
    private int scene = -1;
    @SerializedName(SCENES)
    private Scene[] scenes;

    /**
     * From where the main file was loaded - this is needed for loading assets
     */
    transient private String path;
    /**
     * The filename, minus path
     */
    transient private String filename;
    /**
     * Set to true to render Tangent, Bitangent and Normal buffers using lines
     */
    transient static public boolean debugTBN = true;

    /**
     * Copies the list of Buffers.
     * 
     * @param buffers Buffers are copied here, may be null to create new List.
     * @return buffers
     * 
     */
    public ArrayList<Buffer> getBuffers(ArrayList<Buffer> buffers) {
        if (buffers == null) {
            buffers = new ArrayList<>();
        }
        buffers.addAll(this.buffers);
        return buffers;
    }

    /**
     * Returns the buffer at the specified index
     * 
     * @param index 0 to buffercount
     * @return
     */
    public Buffer getBuffer(int index) {
        return buffers.get(index);
    }

    /**
     * Adds the Buffer to this asset, the Buffer index is returned - this shall be set to BufferViews referencing this
     * Buffer.
     * 
     * @param buffer
     * @return
     */
    protected int addBuffer(Buffer buffer) {
        if (buffers == null) {
            buffers = new ArrayList<>();
        }
        int size = buffers.size();
        buffers.add(buffer);
        return size;
    }

    /**
     * Creates a new BufferView with specified size and a BufferView for the created buffer
     * Use this when a BufferView shall be created for a new buffer.
     * 
     * @param bufferName Name of buffer and bufferview
     * @param bufferSize
     * @param byteOffset
     * @param byteStride
     * @param target
     * @return
     */
    public BufferView createBufferView(String bufferName, int bufferSize, int byteOffset, int byteStride,
            Target target) {
        Buffer buffer = new Buffer(bufferName, bufferSize);
        int index = addBuffer(buffer);
        BufferView bv = new BufferView(this, index, byteOffset, byteStride, target);
        bv.name = bufferName;
        return bv;
    }

    /**
     * Creates a new BufferView using the specified buffer
     * Name of BufferView will be taken from buffer
     * 
     * @param buffer
     * @param byteOffset
     * @param byteStride
     * @param target
     * @return
     */
    public BufferView createBufferView(Buffer buffer, int byteOffset, int byteStride, Target target) {
        int index = buffers.indexOf(buffer);
        if (index < 0) {
            throw new IllegalArgumentException("Could not find index for Buffer - not added?");
        }
        BufferView bv = new BufferView(this, index, byteOffset, byteStride, target);
        bv.name = buffer.getName();
        return bv;
    }

    /**
     * Returns the buffer for the specified accessor
     * 
     * @param accessor
     * @return
     */
    public Buffer getBuffer(Accessor accessor) {
        return buffers.get(bufferViews[accessor.getBufferViewIndex()].getBufferIndex());
    }

    public BufferView[] getBufferViews() {
        return bufferViews;
    }

    /**
     * Returns the number of defined cameras in the gltf asset
     * 
     * @return
     */
    public int getCameraCount() {
        return cameras != null ? cameras.size() : 0;
    }

    /**
     * Returns the camera with the specified index
     * 
     * @param index The index of the camera in the gltf asset to return, 0 - {@link #getCameraCount()}
     * @return The camera or null
     */
    public Camera getCamera(int index) {
        if (cameras != null && index >= 0 && index < cameras.size()) {
            return cameras.get(index);
        }
        return null;
    }

    /**
     * Adds a camera to the gltf asset
     * 
     * @param camera
     * @return The index, as used when calling {@link #getCamera(int)}
     * @throws IllegalArgumentException If camera is null
     */
    public int addCamera(Camera camera) {
        if (camera == null) {
            throw new IllegalArgumentException("Camera is null");
        }
        if (cameras == null) {
            cameras = new ArrayList<>();
        }
        synchronized (cameras) {
            int index = cameras.size();
            cameras.add(camera);
            return index;
        }
    }

    /**
     * 
     * @return
     */
    public Material[] getMaterials() {
        return materials;
    }

    /**
     * Sets the path of the folder where this gltf asset is
     * 
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets the filename, minus path - only set this when gltf is loaded
     * 
     * @param filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Returns the full filename including path
     * 
     * @return
     */
    public String getFilename() {
        return path + File.separatorChar + filename;
    }

    /**
     * Returns the full path to the uri, ie
     * path + File.separatorChar + uri
     * 
     * @param uri
     * @return The full path to the uri, with gltf path prepended.
     */
    public String getPath(String uri) {
        return path + File.separatorChar + uri;
    }

    public Asset getAsset() {
        return asset;
    }

    /**
     * Returns the array holding the scenes
     * 
     * @return
     */
    public Scene[] getScenes() {
        return scenes;
    }

    /**
     * Returns the array of defined images, or null if none used
     * 
     * @return
     */
    public Image[] getImages() {
        return images;
    }

    /**
     * Returns the array of defined samplers, or null if none used
     * 
     * @return
     */
    public Sampler[] getSamplers() {
        return samplers;
    }

    /**
     * Returns the array of defined textures, or null if none used
     * 
     * @return
     */
    public Texture[] getTextures() {
        return textures;
    }

    /**
     * Returns the scene for the specified index, or null if invalid index
     * 
     * @param index
     * @return The scene for the index or null
     */
    public Scene getScene(int index) {
        if (scenes != null && index >= 0 && index < scenes.length) {
            return scenes[index];
        }
        return null;
    }

    /**
     * Returns the value of the default scene, or the first defined scene if default scene has not been specified.
     * 
     * @return The default scene or the first scene in the defined scenes list, or null if no scenes are defined.
     */
    public Scene getDefaultScene() {
        return scene >= 0 ? scenes[scene] : scenes != null ? scenes[0] : null;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public Mesh[] getMeshes() {
        return meshes;
    }

    /**
     * Returns the array of Accessors
     * 
     * @return
     */
    public ArrayList<Accessor> getAccessors() {
        return accessors;
    }

    /**
     * Returns the Accessor at the specified index, or null if invalid index or no Accessors in this asset.
     * 
     * @param index
     * @return
     */
    public Accessor getAccessor(int index) {
        if (accessors != null && index >= 0 && index < accessors.size()) {
            return accessors.get(index);
        }
        return null;
    }

    /**
     * If pbr is not null and pbr has base color texture then the Texture is returned, otherwise null.
     * 
     * @param pbr
     * @return Texture for the pbr or null
     */
    public Texture getTexture(PBRMetallicRoughness pbr) {
        if (pbr != null && pbr.getBaseColorTexture() != null) {
            return textures[pbr.getBaseColorTexture().getIndex()];
        }
        return null;
    }

    /**
     * Returns the texture unit to use with the pbr, or -1 if not specified.
     * 
     * @param pbr
     * @return
     */
    public int getTexCoord(PBRMetallicRoughness pbr) {
        if (pbr != null && pbr.getBaseColorTexture() != null) {
            return pbr.getBaseColorTexture().getTexCoord();
        }
        return -1;

    }

    /**
     * Returns the BufferView for the index, or null if invalid index or no BufferViews in this asset.
     * 
     * @param index
     * @return
     */
    public BufferView getBufferView(int index) {
        if (bufferViews != null && index >= 0 && index < bufferViews.length) {
            return bufferViews[index];
        }
        return null;
    }

    /**
     * Resolves all glTF objects so they can be used without reference to glTF asset
     * Call this method only once, normally done when glTF is loaded using the {@link Loader}
     * 
     * @throws GLTFException If an instance to resolve already has been resolved, ie method has already been called.
     * For instance if the {@link Loader} is used.
     */
    public void resolve() throws GLTFException {
        List<RuntimeResolver> resolves = getResolves();
        for (RuntimeResolver rr : resolves) {
            rr.resolve(this);
        }
    }

    /**
     * Searches through the node hiearchy and returns the first found Node that references a camera or null if none
     * defined.
     * 
     * @param node The starting Node
     * @return Node referencing a Camera or null if none defined in the Node
     */
    public Node getCameraNode(Node node) {
        if (node.getCamera() != null) {
            return node;
        }
        Node[] children = node.getChildren();
        if (children != null) {
            for (Node child : children) {
                Node cameraNode = getCameraNode(child);
                if (cameraNode != null) {
                    return cameraNode;
                }
            }
        }
        return null;
    }

    private List<RuntimeResolver> getResolves() {
        ArrayList<RuntimeResolver> result = new ArrayList<>();
        if (accessors != null) {
            result.addAll(accessors);
        }
        if (bufferViews != null) {
            result.addAll(Arrays.asList(bufferViews));
        }
        if (meshes != null) {
            result.addAll(Arrays.asList(meshes));
        }
        if (textures != null) {
            result.addAll(Arrays.asList(textures));
        }
        // Resolve nodes before scenes
        if (nodes != null) {
            result.addAll(Arrays.asList(nodes));
        }
        if (scenes != null) {
            result.addAll(Arrays.asList(scenes));
        }
        return result;
    }

    /**
     * Deletes all gltf arrays such as Nodes,Scenes, Accessors etc - but does NOT release buffers or images.
     * 
     */
    public void destroy() {
        if (cameras == null) {
            throw new IllegalArgumentException("Already called destroy on GLTF asset");
        }
        accessors = null;
        asset = null;
        destroyBuffers();
        bufferViews = null;
        cameras.clear();
        cameras = null;
        destroyImages();
        materials = null;
        meshes = null;
        nodes = null;
        samplers = null;
        textures = null;
        scenes = null;
    }

    private void destroyBuffers() {
        if (buffers != null) {
            int index = 0;
            for (Buffer buffer : buffers) {
                if (buffer.getBufferName() > 0) {
                    throw new IllegalArgumentException(
                            "Calling destroy on gltf Buffers but has not deleted assets, call AssetManager to delete before calling GLTF.destroy()");
                }
            }
            buffers.clear();
            buffers = null;
        }
    }

    private void destroyImages() {
        if (images != null) {
            int index = 0;
            for (Image image : images) {
                if (image.getTextureName() > 0) {
                    throw new IllegalArgumentException(
                            "Calling destroy on gltf Images but has not deleted assets, call AssetManager to delete before calling GLTF.destroy()");
                }
                images[index] = null;
            }
            images = null;
        }
    }

}
