package com.nucleus.renderer;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import com.nucleus.Backend;
import com.nucleus.Backend.DrawMode;
import com.nucleus.GraphicsPipeline;
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
    }

}
