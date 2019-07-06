package com.nucleus.renderer;

import java.nio.IntBuffer;
import java.util.ArrayList;

import com.nucleus.Backend;
import com.nucleus.Backend.DrawMode;
import com.nucleus.BackendException;
import com.nucleus.CoreApp;
import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.GraphicsPipeline;
import com.nucleus.Pipeline;
import com.nucleus.assets.Assets;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.renderer.BaseRenderer.FrameListener;
import com.nucleus.scene.Node;
import com.nucleus.scene.RenderableNode;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Image;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.scene.gltf.Texture;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.Texture2D;

/**
 * An interface for rendering scenes. This is done by supporting a Node base hierarchy.
 * Parts, or all, of the Nodetree can be rendered by calling {@link #render(Node)} or {@link #render(RootNode)
 * The goal for this API is to provide an abstraction on top of OpenGL/ES so that it can be used without prior
 * OpenGL knowledge.
 * 
 * 
 * @author Richard Sahlin
 *
 */
public interface NucleusRenderer {

    /**
     * The supported renderers
     * 
     */
    public enum Renderers {
        GLES20(2, 0),
        GLES30(3, 0),
        GLES31(3, 1),
        GLES32(3, 2),
        VULKAN10(1, 0),
        VULKAN11(1, 1);

        public final int major;
        public final int minor;

        private Renderers(int major, int minor) {
            this.major = major;
            this.minor = minor;
        };

        /**
         * Returns the enum from major.minor version
         * 
         * @param glVersion
         * @return
         */
        public static Renderers get(int[] glVersion) {
            for (Renderers r : Renderers.values()) {
                if (r.major == glVersion[0] && r.minor == glVersion[1]) {
                    return r;
                }
            }
            return null;
        }

    }

    public enum Matrices {
        MODEL(0),
        VIEW(1),
        PROJECTION(2),
        RENDERPASS_1(4),
        RENDERPASS_2(5);

        public final int index;
        public final static String Name = "uModelMatrix";

        private Matrices(int index) {
            this.index = index;
        }

    }

    /**
     * Min number of stack elements to supports, this is the depth of the tree
     */
    public final static int MIN_STACKELEMENTS = 100;

    /**
     * Layers for the renderer
     * 
     */
    public enum Layer {
        /**
         * Topmost layer
         */
        OVERLAY(0),
        /**
         * Scene layer, the scene nodetree is rendered last in this layer, ie Nodes added to this layer will
         * be rendered before the scene.
         */
        SCENE(1),
        /**
         * The background layer
         */
        BACKGROUND(2);

        public final int index;

        private Layer(int index) {
            this.index = index;
        }

    }

    /**
     * Interface for producer of frames, this is normally only used by the internal implementation, target user is
     * the implementation that takes care of rendering and window handling.
     *
     */
    public interface FrameRenderer extends RenderContextListener {

        /**
         * Produces one whole frame and if needed swaps buffers so that it will be visible.
         * When this method returns the contents for the next frame shall be ready and sent to the graphics layer.
         */
        public void renderFrame();
    }

    /**
     * Keep track of when context is created or lost. This is used by internal implementation and can be used by
     * clients to keep track of when rendering context is created or lost.
     *
     */
    public interface RenderContextListener {

        public final static String INVALID_CONTEXT_DIMENSION = "Illegal size of context: ";

        /**
         * Called when the rendering context is created and ready to be used. Can also be called if
         * EGLcontext is lost and re-created.
         * When this method is called clients must assume that EGL objects are lost (textures) and
         * recreate them.
         * 
         * @param width Width of display in pixels.
         * @param height Height of display in pixels.
         * @throws IllegalArgumentException If width or height <= 0
         */
        public void contextCreated(int width, int height);

        /**
         * Called when the underlying surface (EGL) is lost, this means the context is
         * not valid anymore.
         * If app is restared all context related data must be re-created
         */
        public void surfaceLost();

    }

    /**
     * Called when the context is created for a render surface, EGL/GL is now active and can be used to create
     * objects,
     * textures and buffers.
     * If this method is called again - it means that the EGL context has been lost and is re-created, all EGL
     * resources
     * must be recreated.
     * 
     * @param width Width of display in pixels
     * @param height Height of display in pixels
     */
    public void contextCreated(int width, int height);

    /**
     * Called when the system has resized the window, update viewport and set the new window size to the
     * {@link Window}
     * 
     * @param x Window x start position, normally 0
     * @param y Window y start position, normally 0
     * @param width Window width
     * @param height Window height
     */
    public void resizeWindow(int x, int y, int width, int height);

    /**
     * Call this first time when the context is created, before calling GLContextCreated()
     * Initialize parameters that do not need to be updated when context is re-created.
     * Will set the window size to 0,0,width,height
     * If this method is called more than once nothing is done.
     * 
     * @param surfaceConfig The configuration of the surface in use (from EGL)
     * @param width Width of window surface
     * @param height Height of window surface
     */
    public void init(SurfaceConfiguration surfaceConfig, int width, int height);

    /**
     * Returns the surface configuration
     * 
     * @return The renderers surface configuration (from EGL)
     */
    public SurfaceConfiguration getSurfaceConfiguration();

    /**
     * Signals the start of a frame, implement if needed in subclasses.
     * This must be called by the thread driving rendering.
     * Shall call the framesampler so that the frame delta is updated.
     * Do not perform rendering or time consuming tasks in this method.
     * 
     * @return Number of seconds since last call to beginFrame
     */
    public float beginFrame();

    /**
     * Renders one specific layer or all layers.
     * Uses the current mvp matrix, will call children recursively.
     * This must be called by the thread driving rendering.
     * 
     * @param root The root node to render
     * @throws BackendException If there is a GL error when rendering.
     */
    public void render(RootNode root) throws BackendException;

    /**
     * Signals the end of a frame - rendering is considered to be finished and implementations should call
     * EGL.swapBuffers() if needed
     * This must be called by the thread driving rendering.
     */
    public void endFrame();

    /**
     * Force render mode of objects/meshes
     * Set to null to render meshes normally
     * 
     * @param mode The mode to render meshes with
     */
    public void forceRenderMode(DrawMode mode);

    /**
     * Renders the node using the current mvp matrix, will call children recursively.
     * If node is drawn it will be added to {@link RootNode} list of rendered nodes.
     * 
     * @param node The node to be rendered
     * @throws BackendException If there is an error in GL while drawing this node.
     */
    public void render(RenderableNode<?> node) throws BackendException;

    /**
     * Renders the mesh using the pipeline and matrices
     * 
     * @param pipeline
     * @param mesh
     * @param matrices
     * @throws BackendException
     */
    public void renderMesh(GraphicsPipeline pipeline, Mesh mesh, float[][] matrices) throws BackendException;

    /**
     * Renders the GLTF primitive
     * 
     * @param pipeline
     * @param glTF
     * @param primitive
     * @param matrices
     * @throws BackendException
     */
    public void renderPrimitive(GraphicsPipeline pipeline, GLTF glTF, Primitive primitive, float[][] matrices)
            throws BackendException;

    /**
     * Sets attrib pointers and draws indices or arrays - uniforms must be uploaded to GL before calling this method.
     * 
     * @param pipeline
     * @param indices
     * @param vertexCount
     * @param attribs
     * @param accessors
     * @param mode
     * @throws BackendException
     */
    public void drawVertices(GraphicsPipeline pipeline, Accessor indices, int vertexCount,
            ArrayList<Attributes> attribs, ArrayList<Accessor> accessors, DrawMode mode) throws BackendException;

    /**
     * Returns true if this renderer has been initialized by calling init() when
     * the context is created.
     * 
     * @return
     */
    public boolean isInitialized();

    /**
     * Adds a listener for render context created, if listener is already added nothing is done.
     * TODO Add to {@link CoreApp} or {@link ClientApplication} - normal usage should not need to access
     * NucleusRenderer
     * 
     * @param listener Listener to get callback when render context is created.
     */
    @Deprecated
    public void addContextListener(RenderContextListener listener);

    /**
     * Adds a listener for frame callback, this will be called when beginFrame() is called.
     * Use this when behavior needs to be driven by rendering.
     * 
     * @param listener The listener to get callback before a frame is rendered.
     */
    @Deprecated
    public void addFrameListener(FrameListener listener);

    /**
     * Returns the api backend
     * 
     * @return
     */
    public Backend getBackend();

    /**
     * Sets the projection matrix, this will copy the values from the source matrix.
     * Please not that this will be overwritten when rendering nodes that have a projections.
     * This method can be used when a Node shall be rendered that does not have a projection property.
     * 
     * @param matrix The projection matrix
     * @param index Index into array where matrix is
     * @throws NullPointerException If matrix is null
     * @throws IndexOutOfBoundsException If there is not enough storage in the source matrix at index
     */
    public void setProjection(float[] matrix, int index);

    /**
     * Returns the current render state settings, changing this will not update the opengl settings.
     * Can be used to check current renderstate.
     * 
     * @return
     */
    public RenderState getRenderState();

    /**
     * Uploads the image(s) to the texture, checks if mipmaps should be created.
     * The size of the image will be set in the texture. Texture object must have texture name allocated.
     * 
     * @param texture The texture object, shall have texture name set
     * @param textureImages Array with one or more images to send to GL. If more than
     * one image is specified then multiple mip-map levels will be set.
     * Level 0 shall be at index 0
     * @throws BackendException If there is an error uploading the textures
     * @throws IllegalArgumentException If multiple mipmaps provided but texture min filter is not _MIPMAP_
     * @throws IllegalArgumentException If texture does not have a GL texture name
     */
    public void uploadTextures(Texture2D texture, BufferImage[] textureImages) throws BackendException;

    /**
     * Uploads the image(s) to the texture, checks if mipmaps should be created.
     * 
     * @param image The glTF Image
     * @param true to generate mipmaps
     * @throws BackendException If there is an error uploading the textures
     * @throws IllegalArgumentException If multiple mipmaps provided but texture min filter is not _MIPMAP_
     * @throws IllegalArgumentException If texture does not have a GL texture name
     */
    public void uploadTextures(Image image, boolean generateMipmaps) throws BackendException;

    /**
     * Activates texturing, binds the texture and sets texture parameters
     * Checks if texture is an id (dynamic) reference and sets the texture name if not present.
     * 
     * @param texture
     * @param unit The texture unit number to use, 0 and up
     */
    public void prepareTexture(Texture2D texture, int unit) throws BackendException;

    /**
     * Activates texturing, binds the texture and sets texture parameters
     * Checks if texture is an id (dynamic) reference and sets the texture name if not present.
     * 
     * @param texture Texture to prepare or null
     * @param unit The texture unit number to use, 0 and up
     * @param accessor
     * @param attribute
     * @param texUniform
     * @param samplerUniformBuffer
     */
    public void prepareTexture(Texture texture, int unit, Accessor accessor, NamedShaderVariable attribute,
            NamedShaderVariable texUniform, IntBuffer samplerUniformBuffer) throws BackendException;

    /**
     * Enable the pipeline
     * 
     * @param pipeline
     * @return true if pipeline was changed, ie previously used a different pipeline
     */
    public boolean usePipeline(GraphicsPipeline pipeline) throws BackendException;

    /**
     * Deletes the shaders/program used for the pipeline
     * 
     * @param pipeline
     */
    public void deletePipeline(Pipeline pipeline);

    /**
     * Returns the factory that shall be used to create buffers
     * 
     * @return
     */
    public BufferFactory getBufferFactory();

    /**
     * Returns the assets manager for the renderer, this shall be used to load/fetch resource such as textures and
     * shaders
     * 
     * @return The assets manager for the renderer
     */
    public Assets getAssets();

}
