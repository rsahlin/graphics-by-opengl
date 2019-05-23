package com.nucleus.opengl;

import java.nio.Buffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nucleus.SimpleLogger;
import com.nucleus.assets.AssetManager;
import com.nucleus.common.Constants;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES_EXTENSION_TOKENS;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderPass;
import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderState.Cullface;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.Attachement;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.renderer.SurfaceConfiguration;
import com.nucleus.renderer.Window;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.State;
import com.nucleus.scene.RenderableNode;
import com.nucleus.scene.RootNode;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.ShadowPass1Program;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureUtils;
import com.nucleus.vecmath.Matrix;

/**
 * Platform agnostic renderer, this handles pure render related methods.
 * It uses a GL wrapper to access GL functions, the caller should not need to know the specifics of OpenGLES.
 * The goal of this class is to have a low level renderer that can be used to draw objects on screen without having
 * to access OpenGLES methods directly.
 * This class does not create thread to drive rendering, that shall be done separately.
 * 
 */
public class BaseRenderer implements NucleusRenderer {

    public final static String NOT_INITIALIZED_ERROR = "Not initialized, must call init()";

    protected final static String BASE_RENDERER_TAG = "BaseRenderer";

    private final static String NULL_GLESWRAPPER_ERROR = "GLES wrapper is null";

    private final static int FPS_SAMPLER_DELAY = 5;

    protected SurfaceConfiguration surfaceConfig;

    protected ArrayDeque<float[]> matrixStack = new ArrayDeque<float[]>(MIN_STACKELEMENTS);
    protected ArrayDeque<float[]> projection = new ArrayDeque<float[]>(MIN_STACKELEMENTS);
    protected ArrayDeque<Pass> renderPassStack = new ArrayDeque<>();
    // TODO - move this into a class together with render pass deque so that access of stack and current pass
    // is handled consistently
    private Pass currentPass;
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

    protected GLES20Wrapper gles;
    private Set<RenderContextListener> contextListeners = new HashSet<RenderContextListener>();
    private Set<FrameListener> frameListeners = new HashSet<BaseRenderer.FrameListener>();

    private FrameSampler timeKeeper = FrameSampler.getInstance();
    private float deltaTime;

    protected Window window = Window.getInstance();

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
     * Copy of applied renderstate
     */
    protected RenderState renderState = new RenderState();

    /**
     * Creates a new renderer using the specified GLES20Wrapper
     * 
     * @param gles The gles wrapper
     * @throws IllegalArgumentException If gles is null
     */
    public BaseRenderer(GLES20Wrapper gles) {
        if (gles == null) {
            throw new IllegalArgumentException(NULL_GLESWRAPPER_ERROR);
        }
        gles.createInfo();
        this.gles = gles;
        for (int i = 0; i < matrices.length; i++) {
            matrices[i] = Matrix.setIdentity(Matrix.createMatrix(), 0);
        }
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
    public void init(SurfaceConfiguration surfaceConfig, int width, int height) {
        if (initialized) {
            return;
        }
        resizeWindow(0, 0, width, height);
        initialized = true;
        this.surfaceConfig = surfaceConfig;
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

    /**
     * Internal method to apply the rendersettings.
     * 
     * @param state
     * @throws GLException
     */
    private void setRenderState(RenderState state) throws GLException {
        renderState.set(state);
        int flags = state.getChangeFlag();
        if ((flags & RenderState.CHANGE_FLAG_CLEARCOLOR) != 0) {
            float[] clear = state.getClearColor();
            gles.glClearColor(clear[0], clear[1], clear[2], clear[3]);
        }
        if ((flags & RenderState.CHANGE_FLAG_CULLFACE) != 0) {
            // Set GL values.
            if (state.getCullFace() != Cullface.NONE) {
                gles.glEnable(GLES20.GL_CULL_FACE);
                gles.glCullFace(state.getCullFace().value);
            } else {
                gles.glDisable(GLES20.GL_CULL_FACE);
            }
        }
        if ((flags & RenderState.CHANGE_FLAG_DEPTH) != 0) {
            if (state.getDepthFunc() != GLES20.GL_NONE) {
                gles.glEnable(GLES20.GL_DEPTH_TEST);
                gles.glDepthFunc(state.getDepthFunc());
                gles.glDepthMask(true);
                gles.glClearDepthf(state.getClearDepth());
                gles.glDepthRangef(state.getDepthRangeNear(), state.getDepthRangeFar());
            } else {
                gles.glDisable(GLES20.GL_DEPTH_TEST);
                gles.glClearDepthf(state.getClearDepth());
                gles.glDepthRangef(state.getDepthRangeNear(), state.getDepthRangeFar());
            }
        }
        if ((flags & RenderState.CHANGE_FLAG_MULTISAMPLE) != 0) {
            if (GLES20Wrapper.getInfo()
                    .hasExtensionSupport(GLESWrapper.GLES_EXTENSIONS.ARB_multisample.name())) {
                if (surfaceConfig != null && surfaceConfig.getSamples() > 1 && state.isMultisampling()) {
                    gles.glEnable(GLES_EXTENSION_TOKENS.MULTISAMPLE_EXT.value);
                } else {
                    gles.glDisable(GLES_EXTENSION_TOKENS.MULTISAMPLE_EXT.value);
                }
            }
        }
        GLUtils.handleError(gles, "setRenderSettings ");
    }

    @Override
    public void endFrame() {
    }

    @Override
    public void render(RenderableNode<?> node) throws GLException {
        Pass pass = node.getPass();
        if (pass != null && (currentPass.getFlags() & pass.getFlags()) != 0) {
            // Node has defined pass and masked with current pass
            State state = node.getState();
            if (state == null || state == State.ON || state == State.RENDER) {
                ArrayList<RenderPass> renderPasses = node.getRenderPass();
                if (renderPasses != null) {
                    for (RenderPass renderPass : renderPasses) {
                        if (renderPass.getViewFrustum() != null && renderPass.getPass() == Pass.SHADOW1) {
                            // Save light projection
                            ShadowPass1Program.getLightMatrix(matrices[Matrices.RENDERPASS_1.index]);
                            // Store shadow1 projection * lightmatrix
                            Matrix.mul4(renderPass.getViewFrustum().getMatrix(tempMatrix),
                                    matrices[Matrices.RENDERPASS_1.index],
                                    matrices[Matrices.RENDERPASS_2.index]);
                        }
                        pushPass(renderPass.getPass());
                        setRenderPass(renderPass);
                        // Render node and children
                        internalRender(node);
                        popPass();
                    }
                } else {
                    // Render node and children
                    internalRender(node);
                }
            }
        }
    }

    /**
     * Internal method to render the {@link RenderableNode} using result matrices.
     * If node is rendered it is added to list of rendered nodes in RootNode.
     * Will recursively render child nodes.
     * 
     * @param node
     * @throws GLException
     */
    private void internalRender(RenderableNode<?> node) throws GLException {
        float[] nodeMatrix = node.concatModelMatrix(this.modelMatrix);
        // Fetch projection just before render
        float[] projection = node.getProjection(currentPass);
        if (projection != null) {
            pushMatrix(this.projection, matrices[Matrices.PROJECTION.index]);
            matrices[Matrices.PROJECTION.index] = projection;
        }
        matrices[Matrices.MODEL.index] = nodeMatrix;
        matrices[Matrices.VIEW.index] = viewMatrix;

        NodeRenderer<RenderableNode<?>> nodeRenderer = (NodeRenderer<RenderableNode<?>>) node.getNodeRenderer();
        if (nodeRenderer != null) {
            if (nodeRenderer.renderNode(this, node, currentPass, matrices)) {
                // Add this to rendered nodes before children.
                node.getRootNode().addRenderedNode(node);
            }
        }

        this.modelMatrix = nodeMatrix;
        for (Node n : node.getChildren()) {
            if (n instanceof RenderableNode<?>) {
                pushMatrix(matrixStack, this.modelMatrix);
                render((RenderableNode<?>) n);
                this.modelMatrix = popMatrix(matrixStack);
            }
        }
        if (projection != null) {
            matrices[Matrices.PROJECTION.index] = popMatrix(this.projection);
        }
    }

    private void setupRenderTarget(RenderTarget target) throws GLException {
        boolean init = false;
        if (target.getFramebufferName() == Constants.NO_VALUE && target.getAttachements() != null) {
            createBuffers(target);
            init = true;
        }
        switch (target.getTarget()) {
            case FRAMEBUFFER:
                bindFramebuffer(target);
                break;
            case TEXTURE:
                bindTextureFramebuffer(target);
                break;
            default:
                throw new IllegalArgumentException("Not implemented");
        }
        if (init) {
            initAttachements(target);
        }
    }

    /**
     * Initialize, ie clear to start values, the targets after the buffers are created and target bound
     * 
     * @param target
     */
    private void initAttachements(RenderTarget target) {
        int mask = 0;
        for (AttachementData ad : target.getAttachements()) {
            float[] initValue = ad.getInitValue();
            if (initValue != null) {
                switch (ad.getAttachement()) {
                    case COLOR:
                        mask |= GLES20.GL_COLOR_BUFFER_BIT;
                        gles.glClearColor(initValue[0], initValue[1], initValue[2], initValue[3]);
                        break;
                    default:
                        throw new IllegalArgumentException("Not implemented for attachement " + ad.getAttachement());
                }
            }
        }
        if (mask != 0) {
            gles.glClear(mask);
        }
    }

    /**
     * Binds the specified attachement points as framebuffer render targets
     * 
     * @param target
     */
    private void bindTextureFramebuffer(RenderTarget target) throws GLException {
        // Loop through all attachments and setup, if not defined in rendertarget then disable
        for (Attachement a : Attachement.values()) {
            bindTextureFramebuffer(target, a);
        }
    }

    /**
     * Bind the texture as rendertarget for the specified attachement point
     * 
     * @param target
     * @param attachement
     * @throws GLException
     */
    private void bindTextureFramebuffer(RenderTarget target, Attachement attachement) throws GLException {
        AttachementData ad = target.getAttachement(attachement);
        if (ad == null) {
            disable(attachement);
        } else {
            Texture2D texture = ad.getTexture();
            TextureUtils.prepareTexture(gles, texture, Texture2D.TEXTURE_0);
            gles.bindFramebufferTexture(texture, target.getFramebufferName(), attachement);
            gles.glViewport(0, 0, texture.getWidth(), texture.getHeight());
            enable(attachement);
        }
    }

    private void disable(Attachement attachement) throws GLException {
        switch (attachement) {
            case COLOR:
                gles.glColorMask(false, false, false, false);
                break;
            case DEPTH:
                gles.glDisable(GLES20.GL_DEPTH_TEST);
                break;
            case STENCIL:
                gles.glDisable(GLES20.GL_STENCIL_TEST);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + attachement);

        }
        GLUtils.handleError(gles, "glDisable " + attachement);
    }

    private void enable(Attachement attachement) throws GLException {
        switch (attachement) {
            case COLOR:
                gles.glColorMask(true, true, true, true);
                break;
            case DEPTH:
                gles.glEnable(GLES20.GL_DEPTH_TEST);
                gles.glDepthFunc(GLES20.GL_ALWAYS);
                gles.glDepthMask(true);

                break;
            case STENCIL:
                gles.glEnable(GLES20.GL_STENCIL_TEST);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + attachement);

        }
        GLUtils.handleError(gles, "glDisable " + attachement);

    }

    /**
     * Creates and initializes the buffers needed for the rendertarget
     * 
     * @param target
     */
    private void createBuffers(RenderTarget target) throws GLException {
        ArrayList<AttachementData> attachements = target.getAttachements();
        if (attachements == null) {
            // No attachements - what does this mean?
            SimpleLogger.d(getClass(), "No attachements");
        } else {
            if (target.getFramebufferName() == Constants.NO_VALUE) {
                target.setFramebufferName(createFramebuffer());
            }
            for (AttachementData ad : target.getAttachements()) {
                switch (ad.getAttachement()) {
                    case COLOR:
                    case DEPTH:
                    case STENCIL:
                        createAttachementBuffer(target, ad);
                        break;
                    default:
                        throw new IllegalArgumentException("Not implemented");
                }
            }
        }
    }

    /**
     * Binds framebuffer to the specified target and attachement.
     * Currently only supports binding to window framebuffer (0)
     * 
     * @param target Null target or target with empty/null attachements
     */
    private void bindFramebuffer(RenderTarget target) throws GLException {
        if (target == null || target.getAttachements() == null || target.getAttachements().size() == 0) {
            // Bind default windowbuffer
            gles.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            gles.glViewport(0, 0, window.getWidth(), window.getHeight());
            enable(Attachement.COLOR);
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

    /**
     * Initializes the buffer needed for the attachement
     * Currently only supports render to texture
     * 
     * @param renderTarget
     * @param attachementData
     * @throws GLException
     */
    private void createAttachementBuffer(RenderTarget renderTarget, AttachementData attachementData)
            throws GLException {
        switch (renderTarget.getTarget()) {
            case TEXTURE:
                attachementData.setTexture(createTexture(renderTarget, attachementData));
                break;
            default:
                throw new IllegalArgumentException("Not implemented for target:" + renderTarget.getTarget());
        }
    }

    private int createFramebuffer() {
        final int[] frameBuffer = new int[1];
        gles.glGenFramebuffers(frameBuffer);
        return frameBuffer[0];
    }

    /**
     * Creates a texture name and texture image for the attachement data
     * Will create texture image based on the scale factor in the attachement data
     * 
     * @param renderTarget
     * @param attachementData
     * @return Texture object name
     * @throws GLException
     */
    private Texture2D createTexture(RenderTarget renderTarget, AttachementData attachementData) throws GLException {
        return AssetManager.getInstance().createTexture(this, renderTarget, attachementData);
    }

    private void setRenderPass(RenderPass renderPass) throws GLException {
        // First set state so that rendertargets can override enable/disable writing to buffers
        if (renderPass.getPass() == null || renderPass.getTarget() == null) {
            throw new IllegalArgumentException(
                    "RenderPass must contain pass and target:" + renderPass.getPass() + ", " + renderPass.getTarget());
        }
        RenderState state = renderPass.getRenderState();
        if (state != null) {
            // TODO - check diff between renderpasses and only update accordingly
            state.setChangeFlag(RenderState.CHANGE_FLAG_ALL);
            setRenderState(state);
            state.setChangeFlag(RenderState.CHANGE_FLAG_NONE);
        }
        setupRenderTarget(renderPass.getTarget());
        // Clear buffer according to settings
        int clearFunc = state.getClearFunction();
        if (clearFunc != GLES20.GL_NONE) {
            gles.glClear(clearFunc);
        }
        switch (renderPass.getPass()) {
            case SHADOW2:
                // Adjust the light matrix to fit inside texture coordinates
                // The scale and translate shall be taken from current viewfrustum
                Matrix.setIdentity(matrices[Matrices.RENDERPASS_2.index], 0);
                Matrix.scaleM(matrices[Matrices.RENDERPASS_2.index], 0, 0.5f, 0.5f, 1f);
                Matrix.translate(matrices[Matrices.RENDERPASS_2.index], 0.5f, 0.5f, 0f);
                Matrix.mul4(matrices[Matrices.RENDERPASS_1.index], matrices[Matrices.RENDERPASS_2.index], tempMatrix);
                System.arraycopy(tempMatrix, 0, matrices[Matrices.RENDERPASS_1.index], 0, Matrix.MATRIX_ELEMENTS);
                break;
            default:
                // Nothing to do
        }
    }

    /**
     * 
     * @param node The node being rendered
     * @param pass The currently defined pass
     * @return
     */
    private ShaderProgram getProgram(RenderableNode<?> node, Pass pass) {
        ShaderProgram program = node.getProgram();
        if (program == null) {
            throw new IllegalArgumentException("No program for node " + node.getId());
        }
        return program.getProgram(getGLES(), pass, program.getShading());
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
    public GLES20Wrapper getGLES() {
        if (!initialized) {
            throw new IllegalStateException(NOT_INITIALIZED_ERROR);
        }
        return gles;
    }

    /**
     * Internal method to handle matrix stack, push a matrix on the stack
     * 
     * @param stack The stack to push onto
     * @param matrix
     */
    protected void pushMatrix(ArrayDeque<float[]> stack, float[] matrix) {
        stack.push(matrix);
    }

    /**
     * Internal method to handle matrix stack - pops the latest matrix off the stack
     * 
     * @param stack The stack to pop from
     * @return The poped matrix
     */
    protected float[] popMatrix(ArrayDeque<float[]> stack) {
        return stack.pop();
    }

    @Override
    public void render(RootNode root) throws GLException {
        long start = System.currentTimeMillis();
        List<Node> scene = root.getChildren();
        if (scene != null) {
            for (Node node : scene) {
                render((RenderableNode<?>) node);
            }
        }
        timeKeeper.addTag(FrameSampler.Samples.RENDERNODES, start, System.currentTimeMillis());
    }

    @Override
    public void deleteBuffers(int count, int[] names, int offset) {
        gles.glDeleteBuffers(count, names, offset);
    }

    @Override
    public void bindBuffer(int target, int buffer) {
        gles.glBindBuffer(target, buffer);
    }

    @Override
    public void bufferData(int target, int size, Buffer data, int usage) {
        gles.glBufferData(target, size, data, usage);
    }

    @Override
    public void resizeWindow(int x, int y, int width, int height) {
        Window.getInstance().setSize(width, height);
    }

    @Override
    public void setProjection(float[] matrix, int index) {
        System.arraycopy(matrix, index, matrices[Matrices.PROJECTION.index], 0, 16);
    }

    @Override
    public SurfaceConfiguration getSurfaceConfiguration() {
        return surfaceConfig;
    }

    @Override
    public RenderState getRenderState() {
        return renderState;
    }

}