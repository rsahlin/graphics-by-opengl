package com.nucleus.renderer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.TimeKeeper;
import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.scene.Node;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.ImageFactory;
import com.nucleus.texturing.Texture2D;
import com.nucleus.vecmath.Matrix;

/**
 * Platform agnostic renderer, this handles pure render related methods.
 * It uses a GL wrapper to access GL functions, the caller should not need to know the specifics of OpenGLES.
 * The goal of this class is to have a low level renderer that can be used to draw objects on screen without having
 * to access OpenGLES methods directly.
 * This class does not create thread to drive rendering, that shall be done separately.
 * 
 * @author Richard Sahlin
 */
class BaseRenderer implements NucleusRenderer {

    public final static String NOT_INITIALIZED_ERROR = "Not initialized, must call init()";

    protected final static String BASE_RENDERER_TAG = "BaseRenderer";
    private final static int MIN_STACKELEMENTS = 100;

    private final static String NULL_GLESWRAPPER_ERROR = "GLES wrapper is null";
    private final static String NULL_IMAGEFACTORY_ERROR = "ImageFactory is null";
    private final static String NULL_MATRIXENGINE_ERROR = "MatrixEngine is null";

    protected ViewFrustum viewFrustum = new ViewFrustum();
    protected Deque<float[]> matrixStack = new ArrayDeque<float[]>(MIN_STACKELEMENTS);
    /**
     * The current matrix
     */
    protected float[] mvpMatrix = Matrix.createMatrix();

    protected GLES20Wrapper gles;
    protected ImageFactory imageFactory;
    protected MatrixEngine matrixEngine;
    private ArrayList<RenderContextListener> contextListeners = new ArrayList<RenderContextListener>();
    private ArrayList<FrameListener> frameListeners = new ArrayList<BaseRenderer.FrameListener>();

    private TimeKeeper timeKeeper = new TimeKeeper(30);

    protected Window window = Window.getInstance();

    protected GLInfo glInfo;
    /**
     * Set to true when init is called
     */
    private boolean initialized = false;
    /**
     * Set to true when context is created, if set again it means context was
     * lost and re-created.
     */
    protected boolean contextCreated = false;

    /**
     * Creates a new renderer using the specified GLES20Wrapper
     * 
     * @param gles
     * @throws IllegalArgumentException If gles is null
     * TODO Remove parameters from constructor and move to setter methods, this is in order for injection to be more
     * straightforward
     */
    public BaseRenderer(GLES20Wrapper gles, ImageFactory imageFactory, MatrixEngine matrixEngine) {
        if (gles == null) {
            throw new IllegalArgumentException(NULL_GLESWRAPPER_ERROR);
        }
        if (imageFactory == null) {
            throw new IllegalArgumentException(NULL_IMAGEFACTORY_ERROR);
        }
        if (matrixEngine == null) {
            throw new IllegalArgumentException(NULL_MATRIXENGINE_ERROR);
        }
        this.gles = gles;
        this.imageFactory = imageFactory;
        this.matrixEngine = matrixEngine;
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
    @Override
    public void GLContextCreated(int width, int height) {
        window.setDimension(width, height);
        for (RenderContextListener listener : contextListeners) {
            listener.contextCreated(width, height);
        }
    }

    /**
     * Call this first time when the context is created, before calling GLContextCreated()
     * Initialize parameters that do not need to be updated when context is re-created.
     */
    @Override
    public void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        glInfo = new GLInfo(gles);
    }

    /**
     * Signals the start of a frame, implement if needed in subclasses.
     * This shall be called by the thread driving rendering.
     * 
     * @return Number of seconds since last call to beginFrame
     */
    @Override
    public float beginFrame() {
        float deltaTime = timeKeeper.update();
        if (timeKeeper.getSampleDuration() > 3) {
            System.out.println(BASE_RENDERER_TAG + ": Average FPS: " + timeKeeper.sampleFPS());
        }
        // For now always set the viewport
        // TODO: Add dirty flag in viewport and only set when updated.
        int[] viewport = viewFrustum.getViewPort();
        gles.glViewport(viewport[ViewFrustum.VIEWPORT_X], viewport[ViewFrustum.VIEWPORT_Y],
                viewport[ViewFrustum.VIEWPORT_WIDTH], viewport[ViewFrustum.VIEWPORT_HEIGHT]);
        matrixEngine.setProjectionMatrix(viewFrustum);

        mvpMatrix = getViewFrustum().getProjectionMatrix();

        try {
            // TODO Add render setting with clear flags, depth test, cull face etc.
            gles.glClearColor(0, 0f, 0.4f, 1.0f);
            gles.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            gles.glDisable(GLES20.GL_DEPTH_TEST);
            gles.glDisable(GLES20.GL_CULL_FACE);
            GLUtils.handleError(gles, "Error");

        } catch (GLException e) {
            throw new RuntimeException(e);
        }
        return deltaTime;
    }

    /**
     * Signals the end of a frame - rendering is considered to be finished and implementations should call
     * EGL.swapBuffers() if needed
     * This shall be called by the thread driving rendering.
     */
    @Override
    public void endFrame() {

    }

    /**
     * Call registered FrameListeners to signal that one updated frame shall be produced
     * This shall be called by the thread driving rendering.
     */
    @Override
    public void updateFrame(float deltaTime) {
        for (FrameListener listener : frameListeners) {
            listener.processFrame(deltaTime);
        }

    }

    /**
     * Renders the node using the current mvp matrix, will call children recursively.
     * 
     * @param node The node to be rendered
     * @throws GLException If there is an error in GL while drawing this node.
     */
    @Override
    public void render(Node node) throws GLException {
        float[] modelMatrix = node.getTransform().getMatrix();
        float[] mvp = Matrix.createMatrix();
        Matrix.mul4(mvpMatrix, modelMatrix, mvp);
        renderMeshes(node.getMeshes(), mvp);
        pushMatrix(mvp);
        for (Node n : node.getChildren()) {
            render(n);
        }
        popMatrix();
    }

    /**
     * Renders a list of meshes, this is the same as iterating the list and calling
     * renderMesh() on each mesh.
     * 
     * @param mesh The mesh to be rendered.
     * @param mvpMatrix accumulated matrix for this mesh, this will be sent to uniform.
     * @throws GLException If there is an error in GL while drawing this mesh.
     */
    protected void renderMeshes(ArrayList<Mesh> meshes, float[] mvpMatrix) throws GLException {
        for (Mesh mesh : meshes) {
            renderMesh(mesh, mvpMatrix);
        }
    }

    /**
     * Renders one mesh, material is used to fetch program and set attributes/uniforms.
     * If the attributeupdater is set in the mesh it is called to update buffers.
     * If texture exists in mesh it is made active and used.
     * If mesh contains an index buffer it is used and glDrawElements is called, otherwise
     * drawArrays is called.
     * 
     * @param mesh The mesh to be rendered.
     * @param mvpMatrix accumulated matrix for this mesh, this will be sent to uniform.
     * @throws GLException If there is an error in GL while drawing this mesh.
     */
    protected void renderMesh(Mesh mesh, float[] mvpMatrix) throws GLException {
        AttributeUpdater updater = mesh.getAttributeUpdater();
        if (updater != null) {
            updater.setAttributeData();
        }
        Material material = mesh.getMaterial();
        ShaderProgram program = material.getProgram();
        VertexBuffer vertices = mesh.getVerticeBuffer(0);
        ElementBuffer indices = mesh.getElementBuffer();
        gles.glUseProgram(program.getProgram());

        Texture2D texture = mesh.getTexture(Texture2D.TEXTURE_0);
        if (texture != null) {
            int textureID = texture.getName();
            gles.glActiveTexture(GLES20.GL_TEXTURE0);
            gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
            texture.uploadTexParameters(gles);
        }
        mesh.setBlendModeSeparate(gles);
        program.bindAttributes(gles, mesh);
        program.bindUniforms(gles, mvpMatrix, mesh);

        if (indices == null) {
            gles.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.getVerticeCount());
        } else {
            gles.glDrawElements(indices.getMode().mode, indices.getCount(), indices.getType().type, indices.getBuffer());
        }

        GLUtils.handleError(gles, "glDrawArrays ");
    }

    /**
     * Returns the view frustum
     * 
     * @return
     */
    @Override
    public ViewFrustum getViewFrustum() {
        return viewFrustum;
    }

    /**
     * Returns true if this renderer has been initialized by calling init() when
     * the context is created.
     * 
     * @return
     */
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void createProgram(ShaderProgram program) {
        program.createProgram(gles);
    }

    /**
     * Adds a listener for render context created, if listener is already added nothing is done.
     * 
     * @param listener Listener to get callback when render context is created.
     */
    @Override
    public void addContextListener(RenderContextListener listener) {
        if (contextListeners.contains(listener)) {
            return;
        }
        contextListeners.add(listener);
    }

    /**
     * Adds a listener for frame callback, this will be called when beginFrame() is called.
     * Use this when behavior needs to be driven by rendering.
     * 
     * @param listener The listener to get callback before a frame is rendered.
     */
    @Override
    public void addFrameListener(FrameListener listener) {
        if (frameListeners.contains(listener)) {
            return;
        }
        frameListeners.add(listener);
    }

    /**
     * Returns the GLES20Wrapper.
     * BEWARE - do not use unless you really know what you are doing!!!!
     * 
     * @return The GLES wrapper for GLES functions.
     * @throws IllegalStateException If init() has not been called.
     */
    @Override
    public GLES20Wrapper getGLES() {
        if (!initialized) {
            throw new IllegalStateException(NOT_INITIALIZED_ERROR);
        }
        return gles;
    }

    /**
     * Returns the ImageFactory to be used with this renderer when loading image resources.
     * 
     * @return
     */
    @Override
    public ImageFactory getImageFactory() {
        if (!initialized) {
            throw new IllegalStateException(NOT_INITIALIZED_ERROR);
        }
        return imageFactory;

    }

    /**
     * Internal method to handle matrix stack, push a matrix on the stack
     * 
     * @param matrix
     */
    protected void pushMatrix(float[] matrix) {
        matrixStack.push(matrix);
        mvpMatrix = matrix;
    }

    /**
     * Internal method to handle matrix stack - pops the latest matrix off the stack
     * 
     * @return
     */
    protected float[] popMatrix() {
        mvpMatrix = matrixStack.pop();
        return mvpMatrix;
    }

    @Override
    public void setImageFactory(ImageFactory imageFactory) {
        this.imageFactory = imageFactory;
    }

}
