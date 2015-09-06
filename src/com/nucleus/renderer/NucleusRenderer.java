package com.nucleus.renderer;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.scene.Node;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.ImageFactory;

public interface NucleusRenderer {

    public interface RenderContextListener {
        /**
         * Called when the rendering context is created and ready to be used. Can also be called if
         * context is lost and re-created.
         * When this method is called clients must assume that all objects are lost (textures/programs) and
         * recreate them.
         * 
         * @param width Width of display in pixels.
         * @param height Height of display in pixels.
         */
        public void contextCreated(int width, int height);
    }

    /**
     * Used to drive processing from the render, use this when you need the rendering to drive behavior or other
     * calculations.
     * 
     * @author Richard Sahlin
     *
     */
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
     * Matrix functions that may be accelerated on target platform.
     * 
     * @author Richard Sahlin
     *
     */
    public interface MatrixEngine {

        /**
         * Sets the projection matrix to be used by the renderer based on the setting in the viewFrustum
         * 
         * @param viewFrustum
         * 
         */
        public abstract void setProjectionMatrix(ViewFrustum viewFrustum);

    }

    /**
     * Called when the GL context is created for a render surface, GL is now active and can be used to create objects,
     * textures and buffers.
     * If this method is called again - it means that the GL context has been lost and is re-created, all textures,
     * objects and buffers must be recreated.
     * 
     * @param width Width of display in pixels
     * @param height Height of display in pixels
     */
    public void GLContextCreated(int width, int height);

    /**
     * Call this first time when the context is created, before calling GLContextCreated()
     * Initialize parameters that do not need to be updated when context is re-created.
     */
    public void init();

    /**
     * Sets the scene node to be rendered when {@link #renderScene()} is called
     * 
     * @param scene The scene to be rendered.
     */
    public void setScene(Node scene);

    /**
     * Returns the current scene to be rendered. Take care when updating this in order not to break ongoing rendering.
     * 
     * @return
     */
    public Node getScene();

    /**
     * Signals the start of a frame, implement if needed in subclasses.
     * This shall be called by the thread driving rendering and will call {@link FrameListener#updateGLData()} to copy
     * GL data from sprites.
     * Do not perform rendering or time consuming tasks in this method.
     * 
     * @return Number of seconds since last call to beginFrame
     */
    public float beginFrame();

    /**
     * Renders the scene, this will recursively draw all nodes in the scene.
     * 
     * @throws GLException If there is a GL error when rendering.
     */
    public void renderScene() throws GLException;

    /**
     * Signals the end of a frame - rendering is considered to be finished and implementations should call
     * EGL.swapBuffers() if needed
     * This shall be called by the thread driving rendering.
     */
    public void endFrame();

    /**
     * Renders the node using the current mvp matrix, will call children recursively.
     * 
     * @param node The node to be rendered
     * @throws GLException If there is an error in GL while drawing this node.
     */
    public void render(Node node) throws GLException;

    /**
     * Call {@link FrameListener#processFrame(float)} for registered FrameListeners to signal that one updated frame
     * shall be produced
     * This method may be called from a separate thread from the one doing the rendering.
     * Implementations must take this into consideration.
     */
    public void processFrame();

    /**
     * Returns the view frustum
     * 
     * @return
     */
    public ViewFrustum getViewFrustum();

    /**
     * Returns true if this renderer has been initialized by calling init() when
     * the context is created.
     * 
     * @return
     */
    public boolean isInitialized();

    /**
     * Adds a listener for render context created, if listener is already added nothing is done.
     * 
     * @param listener Listener to get callback when render context is created.
     */
    public void addContextListener(RenderContextListener listener);

    /**
     * Adds a listener for frame callback, this will be called when beginFrame() is called.
     * Use this when behavior needs to be driven by rendering.
     * 
     * @param listener The listener to get callback before a frame is rendered.
     */
    public void addFrameListener(FrameListener listener);

    /**
     * Returns the GLES20Wrapper.
     * BEWARE - do not use unless you really know what you are doing!!!!
     * 
     * @return The GLES wrapper for GLES functions.
     * @throws IllegalStateException If init() has not been called.
     */
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
     * Creates the program object, loads and compiles the shader sources and links the program.
     * 
     * @param program
     * @throws RuntimeException If there is an error loading,compiling or linking the program.
     */
    public void createProgram(ShaderProgram program);

}
