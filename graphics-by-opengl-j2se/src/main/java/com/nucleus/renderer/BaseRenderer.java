package com.nucleus.renderer;

import java.nio.Buffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nucleus.SimpleLogger;
import com.nucleus.assets.AssetManager;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Constants;
import com.nucleus.common.Environment;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES_EXTENSION_TOKENS;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.RenderTarget.Attachement;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.scene.LineDrawerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.NodeTypes;
import com.nucleus.scene.Node.State;
import com.nucleus.scene.RootNode;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.ShadowPass1Program;
import com.nucleus.texturing.ImageFactory;
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
 * @author Richard Sahlin
 */
class BaseRenderer implements NucleusRenderer {

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

    public final static String NOT_INITIALIZED_ERROR = "Not initialized, must call init()";

    protected final static String BASE_RENDERER_TAG = "BaseRenderer";

    private final static String NULL_GLESWRAPPER_ERROR = "GLES wrapper is null";
    private final static String NULL_IMAGEFACTORY_ERROR = "ImageFactory is null";
    private final static String NULL_MATRIXENGINE_ERROR = "MatrixEngine is null";

    private final static int FPS_SAMPLER_DELAY = 5;

    protected SurfaceConfiguration surfaceConfig;

    protected ViewFrustum viewFrustum = new ViewFrustum();

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
     * see Matrices
     */
    protected float[][] matrices = new float[Matrices.values().length][];
    /**
     * The view matrix
     */
    protected float[] viewMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);

    protected GLES20Wrapper gles;
    protected ImageFactory imageFactory;
    protected MatrixEngine matrixEngine;
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
     * Creates a new renderer using the specified GLES20Wrapper
     * 
     * @param gles
     * @throws IllegalArgumentException If gles is null
     * TODO Remove parameters from constructor and move to setter methods, this is in order for injection to be more
     * straightforward
     */
    BaseRenderer(GLES20Wrapper gles, ImageFactory imageFactory, MatrixEngine matrixEngine) {
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
        matrices[Matrices.MODELVIEW.index] = Matrix.createMatrix();
        matrices[Matrices.PROJECTION.index] = Matrix.setIdentity(Matrix.createMatrix(), 0);
        matrices[Matrices.RENDERPASS_1.index] = Matrix.createMatrix();
        matrices[Matrices.RENDERPASS_2.index] = Matrix.createMatrix();
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
        int flags = state.getChangeFlag();
        if ((flags & RenderState.CHANGE_FLAG_CLEARCOLOR) != 0) {
            float[] clear = state.getClearColor();
            gles.glClearColor(clear[0], clear[1], clear[2], clear[3]);
        }
        if ((flags & RenderState.CHANGE_FLAG_CULLFACE) != 0) {
            // Set GL values.
            if (state.getCullFace() != GLES20.GL_NONE) {
                gles.glEnable(GLES20.GL_CULL_FACE);
                gles.glCullFace(state.getCullFace());
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
            if (gles.getInfo()
                    .hasExtensionSupport(GLESWrapper.GLES_EXTENSIONS.multisample_compatibility)) {
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
    public void render(Node node) throws GLException {
        Pass pass = node.getPass();
        if (pass != null && (currentPass.getFlags() & pass.getFlags()) != 0) {
            // Node has defined pass and masked with current pass
            State state = node.getState();
            if (state == null || state == State.ON || state == State.RENDER) {
                ArrayList<RenderPass> renderPasses = node.getRenderPass();
                if (renderPasses != null) {
                    for (RenderPass renderPass : renderPasses) {
                        if (renderPass.getViewFrustum() != null) {
                            Matrix.mul4(ShadowPass1Program.getLightMatrix(matrices[Matrices.RENDERPASS_1.index]),
                                    renderPass.getViewFrustum().getMatrix());
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
     * Internal method to render this node and all children,children are recursively rendered
     * by calling {@link #render(Node)}
     * 
     * @param node
     * @param renderPassMatrix
     * @throws GLException
     */
    private void internalRender(Node node) throws GLException {
        float[] nodeMatrix = node.concatModelMatrix(this.modelMatrix);
        // Fetch projection just before render
        float[] projection = node.getProjection(currentPass);
        if (projection != null) {
            pushMatrix(this.projection, matrices[Matrices.PROJECTION.index]);
            matrices[Matrices.PROJECTION.index] = projection;
        }
        Matrix.mul4(nodeMatrix, viewMatrix, matrices[Matrices.MODELVIEW.index]);
        if (node.getType().equals(NodeTypes.linedrawernode.name())) {
            gles.glLineWidth(((LineDrawerNode) node).getLineWidth());
        }
        renderMeshes(node.getMeshes(), matrices);
        this.modelMatrix = nodeMatrix;
        for (Node n : node.getChildren()) {
            pushMatrix(matrixStack, this.modelMatrix);
            render(n);
            this.modelMatrix = popMatrix(matrixStack);
        }
        if (projection != null) {
            matrices[Matrices.PROJECTION.index] = popMatrix(this.projection);
        }
        node.getRootNode().addRenderedNode(node);
    }

    private void setupRenderTarget(RenderTarget target) throws GLException {
        if (target.getFramebufferName() == Constants.NO_VALUE && target.getAttachements() != null) {
            createBuffers(target);
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
            gles.glViewport(0, 0, window.width, window.height);
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
                Matrix.setIdentity(matrices[Matrices.RENDERPASS_2.index], 0);
                Matrix.scaleM(matrices[Matrices.RENDERPASS_2.index], 0, 0.5f, 0.5f, 1f);
                Matrix.translate(matrices[Matrices.RENDERPASS_2.index], 0.5f, 0.5f, 0f);
                Matrix.mul4(matrices[Matrices.RENDERPASS_1.index], matrices[Matrices.RENDERPASS_2.index]);
                break;
            default:
                // Nothing to do
        }
    }

    protected void renderMeshes(ArrayList<Mesh> meshes, float[][] matrices) throws GLException {
        for (Mesh mesh : meshes) {
            renderMesh(mesh, matrices);
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
     * @param matrices accumulated modelview matrix for this mesh, this will be sent to uniform.
     * projectionMatrix The projection matrix, depending on shader this is either concatenated
     * with modelview set to unifom.
     * renderPassMatrix Optional matrix for renderpass
     * @throws GLException If there is an error in GL while drawing this mesh.
     */
    protected void renderMesh(Mesh mesh, float[][] matrices)
            throws GLException {
        Consumer updater = mesh.getAttributeConsumer();
        if (updater != null) {
            updater.updateAttributeData(this);
        }
        if (mesh.getDrawCount() == 0) {
            return;
        }
        Material material = mesh.getMaterial();
        ShaderProgram program = getProgram(material, currentPass);
        gles.glUseProgram(program.getProgram());
        GLUtils.handleError(gles, "glUseProgram " + program.getProgram());

        material.setBlendModeSeparate(gles);
        program.updateAttributes(gles, mesh);
        program.updateUniforms(gles, matrices, mesh);
        program.prepareTextures(gles, mesh);

        AttributeBuffer vertices = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        ElementBuffer indices = mesh.getElementBuffer();

        // TODO - is this the best place for this check - remember, this should only be done in debug cases.
        if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
            program.validateProgram(getGLES());
        }

        if (indices == null) {
            gles.glDrawArrays(mesh.getMode().mode, mesh.getOffset(), mesh.getDrawCount());
            GLUtils.handleError(gles, "glDrawArrays ");
            timeKeeper.addDrawArrays(mesh.getDrawCount());
        } else {
            if (indices.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
                gles.glDrawElements(mesh.getMode().mode, mesh.getDrawCount(), indices.getType().type,
                        mesh.getOffset());
                GLUtils.handleError(gles, "glDrawElements with ElementBuffer ");
            } else {
                gles.glDrawElements(mesh.getMode().mode, mesh.getDrawCount(), indices.getType().type,
                        indices.getBuffer().position(mesh.getOffset()));
                GLUtils.handleError(gles, "glDrawElements no ElementBuffer ");
            }
            timeKeeper.addDrawElements(vertices.getVerticeCount(), mesh.getDrawCount());
        }
    }

    /**
     * 
     * @param material
     * @param pass The currently defined pass
     * @return
     */
    private ShaderProgram getProgram(Material material, Pass pass) {
        ShaderProgram program = material.getProgram();
        return program.getProgram(this, pass, program.getShading());
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void createProgram(ShaderProgram program) {
        program.createProgram(gles);
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
    public void setImageFactory(ImageFactory imageFactory) {
        this.imageFactory = imageFactory;
    }

    @Override
    public void render(RootNode root) throws GLException {
        long start = System.currentTimeMillis();
        List<Node> scene = root.getChildren();
        if (scene != null) {
            for (Node node : scene) {
                render(node);
            }
        }
        FrameSampler.getInstance().addTag(FrameSampler.Samples.RENDERNODES, start, System.currentTimeMillis());
    }

    @Override
    public void genBuffers(int[] names) {
        gles.glGenBuffers(names);
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
        System.arraycopy(matrix, index, matrices[1], 0, 16);
    }

    @Override
    public SurfaceConfiguration getSurfaceConfiguration() {
        return surfaceConfig;
    }

}
