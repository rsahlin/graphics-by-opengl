package com.nucleus.renderer;

import java.nio.Buffer;
import java.util.ArrayList;

import com.nucleus.CoreApp;
import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.scene.Node;
import com.nucleus.scene.RenderableNode;
import com.nucleus.scene.RootNode;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.ImageFactory;
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
     * 
     * Simple interface for render of node, use this when the node requires changes to the default behavior
     * when rendering the node.
     *
     * @param <T> The node type
     */
    public abstract class NodeRenderer {

        /**
         * Renders the contents of the node for the specified render pass and using the matrices supplied.
         * This method should only render the specified node - traversal of tree is handled in renderer.
         * Do not recurse rendering of children.
         * 
         * @param renderer
         * @param node The node to render
         * @param currentPass Current render pass
         * @param matrices The matrices to use
         * @throws GLException
         */
        public abstract void renderNode(NucleusRenderer renderer, RenderableNode<?> node, Pass currentPass, float[][] matrices)
                throws GLException;

    }

    public enum Matrices {
        MODELVIEW(0),
        PROJECTION(1),
        RENDERPASS_1(2),
        RENDERPASS_2(3);

        public final int index;

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
     * Used to drive processing from the render, use this when you need the rendering to drive behavior or other
     * calculations.
     * 
     * @author Richard Sahlin
     *
     */
    @Deprecated
    public interface FrameListener {
        /**
         * Called when a new frame shall be processed (by the logic)
         * Update objects position, behavior, animation etc based on the deltaTime.
         * Do not update any GL data to keep this thread-safe for GL.
         * 
         * @param deltaTime Time, in seconds, since last frame
         */
        public void processFrame(float deltaTime);

        /**
         * Called after {@link #processFrame(float)} and before rendering to GL.
         * Implementations must update GL data that has changed during the call to {@link #processFrame(float)}
         */
        public void updateGLData();
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
     * @throws GLException If there is a GL error when rendering.
     */
    public void render(RootNode root) throws GLException;

    /**
     * Signals the end of a frame - rendering is considered to be finished and implementations should call
     * EGL.swapBuffers() if needed
     * This must be called by the thread driving rendering.
     */
    public void endFrame();

    /**
     * Renders the node using the current mvp matrix, will call children recursively.
     * If node is drawn it will be added to {@link RootNode} list of rendered nodes.
     * 
     * @param node The node to be rendered
     * @throws GLException If there is an error in GL while drawing this node.
     */
    public void render(Node node) throws GLException;

    /**
     * 
     * @param matrix modelview matrix for the object, will use current projection matrix
     * @param meshes List of meshes to draw
     * @param texture The target texture
     * @param renderpass
     * @throws GLException If there is an error in GL while drawing this node.
     */
    public void renderToTexture(float[] matrix, ShaderProgram program, ArrayList<Mesh> meshes, Texture2D texture,
            RenderPass renderpass)
            throws GLException;

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
     * Returns the GLES20Wrapper.
     * BEWARE - do not use unless you really know what you are doing!!!!
     * 
     * @return The GLES wrapper for GLES functions.
     * @throws IllegalStateException If init() has not been called.
     */
    @Deprecated
    public GLES20Wrapper getGLES();

    /**
     * Returns the ImageFactory to be used with this renderer when loading image resources.
     * 
     * @return
     * @throws IllegalStateException If renderer is not initialized.
     */
    public ImageFactory getImageFactory();

    /**
     * Sets the image factory to use when loading image resources.
     * 
     * @param imageFactory Used when images are loaded.
     */
    public void setImageFactory(ImageFactory imageFactory);

    /**
     * Generate GL named object buffers
     * 
     * @param names Destination for buffer names
     */
    public void genBuffers(int[] names);

    /**
     * Deletes the named object buffers generated with a call to {@link #genBuffers(int, int[], int)}
     * 
     * @param count Number of buffer names to delete
     * @param names Named buffers to delete
     * @param offset Offset into names
     */
    public void deleteBuffers(int count, int[] names, int offset);

    /**
     * Binds the named buffer to the specified target.
     * see OpenGL.glBindBuffer()
     * 
     * @param target
     * @param buffer
     */
    public void bindBuffer(int target, int buffer);

    /**
     * create and initialize a buffer object's data store, from OpenGL.glBufferData()
     * 
     * @param target Specifies the target buffer object. The symbolic constant must be GL_ARRAY_BUFFER or
     * GL_ELEMENT_ARRAY_BUFFER, or any of the targets allowed for the current GL implementation
     * @param size Specifies the size in bytes of the buffer object's new data store.
     * @param data Specifies a pointer to data that will be copied into the data store for initialization, or NULL
     * if no
     * data is to be copied.
     * @param usage Specifies the expected usage pattern of the data store. The symbolic constant must be
     * GL_STREAM_DRAW, GL_STATIC_DRAW, or GL_DYNAMIC_DRAW.
     */
    public void bufferData(int target, int size, Buffer data, int usage);

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

}
