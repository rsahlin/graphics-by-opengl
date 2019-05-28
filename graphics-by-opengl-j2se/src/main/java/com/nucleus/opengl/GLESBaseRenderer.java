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
import com.nucleus.common.Environment;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES_EXTENSION_TOKENS;
import com.nucleus.opengl.shader.GLTFShaderProgram;
import com.nucleus.opengl.shader.ShaderProgram;
import com.nucleus.opengl.shader.ShadowPass1Program;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.Backend;
import com.nucleus.renderer.Backend.DrawMode;
import com.nucleus.renderer.Configuration;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderBackendException;
import com.nucleus.renderer.RenderPass;
import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderState.ClearFunc;
import com.nucleus.renderer.RenderState.Cullface;
import com.nucleus.renderer.RenderState.DepthFunc;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.Attachement;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.renderer.SurfaceConfiguration;
import com.nucleus.renderer.Window;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.State;
import com.nucleus.scene.RenderableNode;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.BufferView;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Image;
import com.nucleus.scene.gltf.Material.AlphaMode;
import com.nucleus.scene.gltf.PBRMetallicRoughness;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.scene.gltf.Texture;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Format;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureType;
import com.nucleus.vecmath.Matrix;

/**
 * Platform agnostic renderer, this handles pure render related methods.
 * It uses a GL wrapper to access GL functions, the caller should not need to know the specifics of OpenGLES.
 * The goal of this class is to have a low level renderer that can be used to draw objects on screen without having
 * to access OpenGLES methods directly.
 * This class does not create thread to drive rendering, that shall be done separately.
 * 
 */
public class GLESBaseRenderer implements NucleusRenderer {

    public final static String NOT_INITIALIZED_ERROR = "Not initialized, must call init()";

    protected final static String BASE_RENDERER_TAG = "BaseRenderer";

    private final static String NULL_GLESWRAPPER_ERROR = "GLES wrapper is null";
    private final static String INVALID_WRAPPER_ERROR = "Render backend wrapper is not instance of GLES";

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
    private Set<FrameListener> frameListeners = new HashSet<GLESBaseRenderer.FrameListener>();
    protected int currentProgram = -1;
    protected Cullface cullFace;
    protected DrawMode forceMode = null;

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
     * Creates a new GLES based renderer
     * 
     * @param backend The render backend wrapper, must be instance of GLESWrapper
     * @throws IllegalArgumentException If gles is null or not instance of GLESWrapper
     */
    public GLESBaseRenderer(Backend backend) {
        if (backend == null) {
            throw new IllegalArgumentException(NULL_GLESWRAPPER_ERROR);
        }
        if (!(backend instanceof GLESWrapper)) {
            throw new IllegalArgumentException(INVALID_WRAPPER_ERROR);
        }
        gles = (GLES20Wrapper) backend;
        gles.createInfo();
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
     * @throws RenderBackendException
     */
    private void setRenderState(RenderState state) throws RenderBackendException {
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
                gles.glCullFace(gles.getCullFace(state.getCullFace()));
            } else {
                gles.glDisable(GLES20.GL_CULL_FACE);
            }
        }
        if ((flags & RenderState.CHANGE_FLAG_DEPTH) != 0) {
            DepthFunc function = state.getDepthFunc();
            if (state.getDepthFunc() != DepthFunc.NONE) {
                gles.glEnable(GLES20.GL_DEPTH_TEST);
                gles.glDepthFunc(gles.getDepthFunc(function));
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
    public void forceRenderMode(DrawMode mode) {
        forceMode = mode != null ? mode : null;
    }

    @Override
    public void render(RenderableNode<?> node) throws RenderBackendException {
        forceMode = Configuration.getInstance().getGLTFMode();
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
     * @throws RenderBackendException
     */
    private void internalRender(RenderableNode<?> node) throws RenderBackendException {
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

    private void setupRenderTarget(RenderTarget target) throws RenderBackendException {
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
    private void bindTextureFramebuffer(RenderTarget target) throws RenderBackendException {
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
     * @throws RenderBackendException
     */
    private void bindTextureFramebuffer(RenderTarget target, Attachement attachement) throws RenderBackendException {
        AttachementData ad = target.getAttachement(attachement);
        if (ad == null) {
            disable(attachement);
        } else {
            Texture2D texture = ad.getTexture();
            prepareTexture(texture, Texture2D.TEXTURE_0);
            gles.bindFramebufferTexture(texture, target.getFramebufferName(), attachement);
            gles.glViewport(0, 0, texture.getWidth(), texture.getHeight());
            enable(attachement);
        }
    }

    private void disable(Attachement attachement) throws RenderBackendException {
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

    private void enable(Attachement attachement) throws RenderBackendException {
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
    private void createBuffers(RenderTarget target) throws RenderBackendException {
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
    private void bindFramebuffer(RenderTarget target) throws RenderBackendException {
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
     * @throws RenderBackendException
     */
    private void createAttachementBuffer(RenderTarget renderTarget, AttachementData attachementData)
            throws RenderBackendException {
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
     * @throws RenderBackendException
     */
    private Texture2D createTexture(RenderTarget renderTarget, AttachementData attachementData)
            throws RenderBackendException {
        return AssetManager.getInstance().createTexture(this, renderTarget, attachementData);
    }

    private void setRenderPass(RenderPass renderPass) throws RenderBackendException {
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
        int clearFunc = state.getClearFlags();
        if (clearFunc != ClearFunc.NONE.flag) {
            gles.glClear(gles.getClearMask(clearFunc));
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
    public void render(RootNode root) throws RenderBackendException {
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

    @Override
    public void uploadTextures(Texture2D texture, BufferImage[] textureImages)
            throws RenderBackendException {
        if (texture.getName() <= 0) {
            throw new IllegalArgumentException("No texture name for texture " + texture.getId());
        }
        long start = System.currentTimeMillis();
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getName());
        boolean isMipMapParams = texture.getTexParams().isMipMapFilter();
        if ((textureImages.length > 1 && !isMipMapParams) || (texture.getLevels() > 1 && !isMipMapParams)) {
            throw new IllegalArgumentException(
                    "Multiple mipmap images but wrong min filter "
                            + texture.getTexParams().getParameters()[TextureParameter.MIN_FILTER_INDEX]);
        }
        int level = 0;
        texture.setup(textureImages[0].getWidth(), textureImages[0].getHeight());
        for (BufferImage textureImg : textureImages) {
            if (textureImg != null) {
                if (texture.getFormat() == null || texture.getType() == null) {
                    throw new IllegalArgumentException("Texture format or type is null for id " + texture.getId()
                            + " : " + texture.getFormat() + ", " + texture.getType());
                }
                gles.texImage(texture, textureImg, level);
                GLUtils.handleError(gles, "texImage2D");
                level++;
            } else {
                break;
            }
        }
        if (textureImages.length == 1 && texture.getTexParams().isMipMapFilter()
                || Configuration.getInstance().isGenerateMipMaps()) {
            gles.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            SimpleLogger.d(TextureUtils.class, "Generated mipmaps for texture " + texture.getId());
        }
        FrameSampler.getInstance().logTag(FrameSampler.Samples.UPLOAD_TEXTURE, texture.getId(), start,
                System.currentTimeMillis());
    }

    @Override
    public void uploadTextures(Image image, boolean generateMipmaps)
            throws RenderBackendException {
        if (image.getTextureName() <= 0) {
            throw new IllegalArgumentException("No texture name for texture " + image.getUri());
        }
        long start = System.currentTimeMillis();
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, image.getTextureName());
        Format format = gles.texImage(image, 0);
        GLUtils.handleError(gles, "texImage2D");
        SimpleLogger.d(TextureUtils.class,
                "Uploaded texture " + image.getUri() + " with format " + format);
        if (generateMipmaps || Configuration.getInstance().isGenerateMipMaps()) {
            gles.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            SimpleLogger.d(TextureUtils.class, "Generated mipmaps for texture " + image.getUri());
        }
        FrameSampler.getInstance().logTag(FrameSampler.Samples.UPLOAD_TEXTURE, " " + image.getUri(), start,
                System.currentTimeMillis());
    }

    @Override
    public void prepareTexture(Texture2D texture, int unit) throws RenderBackendException {
        if (texture != null && texture.textureType != TextureType.Untextured) {
            gles.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
            int textureID = texture.getName();
            if (textureID == Constants.NO_VALUE && texture.getExternalReference().isIdReference()) {
                // Texture has no texture object - and is id reference
                // Should only be used for dynamic textures, eg ones that depend on define in existing node
                AssetManager.getInstance().getIdReference(texture);
                textureID = texture.getName();
                gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
                gles.uploadTexParameters(texture.getTexParams());
                GLUtils.handleError(gles, "glBindTexture()");
            } else {
                gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
                gles.uploadTexParameters(texture.getTexParams());
                GLUtils.handleError(gles, "glBindTexture()");
            }
        }
    }

    @Override
    public void prepareTexture(Texture texture, int unit) throws RenderBackendException {
        if (texture != null) {
            gles.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
            int textureID = texture.getImage().getTextureName();
            gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
            gles.uploadTexParameters(texture);
            GLUtils.handleError(gles, "glBindTexture()");
        }
    }

    @Override
    public int[] createTextureName() {
        int[] textures = new int[1];
        gles.glGenTextures(textures);
        return textures;
    }

    @Override
    public boolean useProgram(ShaderProgram program) throws RenderBackendException {
        if (currentProgram != program.getProgram()) {
            currentProgram = program.getProgram();
            gles.glUseProgram(currentProgram);
            GLUtils.handleError(gles, "glUseProgram " + currentProgram);
            // TODO - is this the best place for this check - remember, this should only be done in debug cases.
            if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
                program.validateProgram(gles);
            }
            return true;
        }
        return false;
    }

    @Override
    public void renderMesh(ShaderProgram program, Mesh mesh, float[][] matrices) throws RenderBackendException {
        Material material = mesh.getMaterial();
        program.setUniformMatrices(matrices);
        program.updateUniformData(program.getUniformData());

        program.updateAttributes(gles, mesh);
        program.uploadUniforms(gles);
        program.prepareTexture(this, mesh.getTexture(Texture2D.TEXTURE_0));
        material.setBlendModeSeparate(gles);
        int mode = gles.getDrawMode(mesh.getMode());
        ElementBuffer indices = mesh.getElementBuffer();
        if (indices == null) {
            gles.glDrawArrays(mode, mesh.getOffset(), mesh.getDrawCount());
            GLUtils.handleError(gles, "glDrawArrays ");
            timeKeeper.addDrawArrays(mesh.getDrawCount());
        } else {
            if (indices.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
                gles.glDrawElements(mode, mesh.getDrawCount(), indices.getType().type,
                        mesh.getOffset());
                GLUtils.handleError(gles, "glDrawElements with ElementBuffer " + mesh.getMode() + ", "
                        + mesh.getDrawCount() + ", " + indices.getType() + ", " + mesh.getOffset());
            } else {
                gles.glDrawElements(mode, mesh.getDrawCount(), indices.getType().type,
                        indices.getBuffer().position(mesh.getOffset()));
                GLUtils.handleError(gles, "glDrawElements no ElementBuffer ");
            }
            AttributeBuffer vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES_STATIC);
            if (vertices == null) {
                vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES);
            }
            timeKeeper.addDrawElements(vertices.getVerticeCount(), mesh.getDrawCount());
        }
        gles.disableAttribPointers();
    }

    @Override
    public void renderPrimitive(GLTFShaderProgram program, GLTF glTF, Primitive primitive, float[][] matrices)
            throws RenderBackendException {
        if (useProgram(program)) {
            program.updateEnvironmentUniforms(gles, glTF.getDefaultScene());
        }
        // Can be optimized to update uniforms under the following conditions:
        // The program has changed OR the matrices have changed, ie another parent node.
        program.setUniformMatrices(matrices);
        program.updateUniformData(program.getUniformData());
        program.uploadUniforms(gles);
        com.nucleus.scene.gltf.Material material = primitive.getMaterial();
        if (material != null) {
            PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
            // Check for doublesided.
            if (material.isDoubleSided() && renderState.getCullFace() != Cullface.NONE) {
                cullFace = renderState.getCullFace();
                gles.glDisable(GLES20.GL_CULL_FACE);
            }
            program.prepareTextures(this, glTF, primitive, material);
            if (material.getAlphaMode() == AlphaMode.OPAQUE) {
                gles.glDisable(GLES20.GL_BLEND);
            } else {
                gles.glEnable(GLES20.GL_BLEND);
            }
        }
        Accessor indices = primitive.getIndices();
        Accessor position = primitive.getAccessor(Attributes.POSITION);
        program.updatePBRUniforms(gles, primitive);
        drawVertices(program, indices, position.getCount(), primitive.getAttributesArray(),
                primitive.getAccessorArray(), forceMode == null ? primitive.getMode() : forceMode);
        // Restore cullface if changed.
        if (cullFace != null) {
            gles.glEnable(GLES20.GL_CULL_FACE);
            cullFace = null;
        }
        gles.disableAttribPointers();
    }

    @Override
    public void drawVertices(ShaderProgram program, Accessor indices, int vertexCount,
            ArrayList<Attributes> attribs, ArrayList<Accessor> accessors, DrawMode mode) throws RenderBackendException {
        gles.glVertexAttribPointer(program, attribs, accessors);
        GLUtils.handleError(gles, "glVertexAttribPointer");
        int modeValue = gles.getDrawMode(mode);
        if (indices != null) {
            // Indexed mode - use glDrawElements
            BufferView indicesView = indices.getBufferView();
            com.nucleus.scene.gltf.Buffer buffer = indicesView.getBuffer();
            if (buffer.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffer.getBufferName());
                GLUtils.handleError(gles, "glBindBuffer");
                gles.glDrawElements(modeValue, indices.getCount(), indices.getComponentType().value,
                        indices.getByteOffset() + indicesView.getByteOffset());
                GLUtils.handleError(gles, "glDrawElements VBO " + buffer.getBufferName());
            } else {
                gles.glDrawElements(modeValue, indices.getCount(), indices.getComponentType().value,
                        indices.getBuffer());
                GLUtils.handleError(gles, "glDrawElements");
            }
            timeKeeper.addDrawElements(indices.getCount(), vertexCount);
        } else {
            // Non indexed mode - use glDrawArrays
            gles.glDrawArrays(modeValue, 0, vertexCount);
            GLUtils.handleError(gles, "glDrawArrays VBO");
            timeKeeper.addDrawArrays(vertexCount);
        }

    }

    @Override
    public void createTexture(Texture2D texture, int target) throws RenderBackendException {
        getGLES().glBindTexture(target, texture.getName());
        getGLES().texImage(texture);
        GLUtils.handleError(getGLES(), "glTexImage2D");
    }

    @Override
    public void deleteTextures(int[] names) {
        getGLES().glDeleteTextures(names);
    }

    @Override
    public void deletePrograms(int[] names) {
        for (int i : names) {
            getGLES().glDeleteProgram(i);
        }
    }

}
