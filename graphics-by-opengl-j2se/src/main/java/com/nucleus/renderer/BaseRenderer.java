package com.nucleus.renderer;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import com.nucleus.Backend;
import com.nucleus.Backend.DrawMode;
import com.nucleus.GraphicsPipeline;
import com.nucleus.SimpleLogger;
import com.nucleus.assets.Assets;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.RenderState.Cullface;
import com.nucleus.vecmath.Matrix;

/**
 * Platform and render backend agnostic renderer - use this to implement renderer based on specific backend such
 * as GLES or Vulkan.
 * 
 */

public abstract class BaseRenderer implements NucleusRenderer {

    public final static String NOT_INITIALIZED_ERROR = "Not initialized, must call init()";
    protected final static String BASE_RENDERER_TAG = "BaseRenderer";
    protected final static String NULL_APIWRAPPER_ERROR = "Backend API wrapper is null";
    protected final static String INVALID_WRAPPER_ERROR = "Render backend wrapper is wrong instance";

    protected final static int FPS_SAMPLER_DELAY = 5;

    protected SurfaceConfiguration surfaceConfig;

    protected ArrayDeque<float[]> matrixStack = new ArrayDeque<float[]>(MIN_STACKELEMENTS);
    protected ArrayDeque<float[]> projection = new ArrayDeque<float[]>(MIN_STACKELEMENTS);
    protected ArrayDeque<Pass> renderPassStack = new ArrayDeque<>();
    // TODO - move this into a class together with render pass deque so that access of stack and current pass
    // is handled consistently
    protected Pass currentPass;
    /**
     * Reference to the current modelmatrix, each Node has its own Matrix that is referenced.
     */
    protected float[] modelMatrix;
    /**
     * see {@link Matrices}
     */
    protected float[][] matrices = new float[Matrices.values().length][];
    /**
     * The view matrix
     */
    protected float[] viewMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);

    /**
     * Temp matrix - not threadsafe
     */
    protected float[] tempMatrix = Matrix.createMatrix();

    protected BufferFactory bufferFactory;
    protected Assets assetManager;
    protected Set<RenderContextListener> contextListeners = new HashSet<RenderContextListener>();
    protected GraphicsPipeline currentPipeline = null;
    protected Cullface cullFace;
    protected DrawMode forceMode = null;

    protected FrameSampler timeKeeper = FrameSampler.getInstance();
    protected float deltaTime;

    protected Window window = Window.getInstance();

    /**
     * Set to true when init is called
     */
    protected boolean initialized = false;
    /**
     * Set to true when context is created, if set again it means context was
     * lost and re-created.
     */
    protected boolean contextCreated = false;
    /**
     * Copy of applied renderstate
     */
    protected RenderState renderState = new RenderState();
    protected Set<FrameListener> frameListeners = new HashSet<FrameListener>();
    protected Backend backend;

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
     * Internal constructor
     * 
     * @param backend
     */
    protected BaseRenderer(Backend backend) {
        if (backend == null) {
            throw new IllegalArgumentException(NULL_APIWRAPPER_ERROR);
        }
        this.backend = backend;
    }

    @Override
    public SurfaceConfiguration getSurfaceConfiguration() {
        return surfaceConfig;
    }

    @Override
    public RenderState getRenderState() {
        return renderState;
    }

    @Override
    public void init(SurfaceConfiguration surfaceConfig, int width, int height) {
        if (initialized) {
            return;
        }
        resizeWindow(0, 0, width, height);
        initialized = true;
        this.surfaceConfig = surfaceConfig;
    }

    @Override
    public void contextCreated(int width, int height) {
        SimpleLogger.d(getClass(), "contextCreated()");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(RenderContextListener.INVALID_CONTEXT_DIMENSION);
        }
        resizeWindow(0, 0, width, height);
        for (RenderContextListener listener : contextListeners) {
            listener.contextCreated(width, height);
        }
    }

    @Override
    public void resizeWindow(int x, int y, int width, int height) {
        Window.getInstance().setSize(width, height);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void addContextListener(RenderContextListener listener) {
        if (contextListeners.contains(listener)) {
            return;
        }
        contextListeners.add(listener);
    }

    @Override
    public void addFrameListener(FrameListener listener) {
        if (frameListeners.contains(listener)) {
            return;
        }
        frameListeners.add(listener);
    }

    @Override
    public void setProjection(float[] matrix, int index) {
        System.arraycopy(matrix, index, matrices[Matrices.PROJECTION.index], 0, 16);
    }

    @Override
    public float beginFrame() {
        renderPassStack.clear();
        pushPass(Pass.UNDEFINED);
        deltaTime = timeKeeper.update();
        if (timeKeeper.getSampleDuration() > FPS_SAMPLER_DELAY) {
            SimpleLogger.d(getClass(), timeKeeper.sampleFPS());
        }
        for (FrameListener listener : frameListeners) {
            listener.processFrame(timeKeeper.getDelta());
            listener.updateGLData();
        }
        this.modelMatrix = null;

        return deltaTime;
    }

    /**
     * Pushes the current pass and sets {@link #currentPass}
     * 
     * @param pass New current pass
     */
    protected void pushPass(Pass pass) {
        if (currentPass != null) {
            renderPassStack.push(currentPass);
        }
        currentPass = pass;
    }

    /**
     * Pops a pass from the stack to {@link #currentPass}
     * 
     * @return The popped pass (same as {@link #currentPass} or null if stack empty.
     */
    protected Pass popPass() {
        currentPass = renderPassStack.pop();
        return currentPass;
    }

    @Override
    public Backend getBackend() {
        return backend;
    }

}
