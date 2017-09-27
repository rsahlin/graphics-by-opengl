package com.nucleus.renderer;

import java.nio.Buffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nucleus.SimpleLogger;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Constants;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.AttributeUpdater.Producer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES_EXTENSIONS;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.RenderTarget.Attachement;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.renderer.RenderTarget.Target;
import com.nucleus.scene.Node;
import com.nucleus.scene.RenderPass;
import com.nucleus.scene.Node.NodeTypes;
import com.nucleus.scene.Node.State;
import com.nucleus.scene.RootNode;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Image.ImageFormat;
import com.nucleus.texturing.ImageFactory;
import com.nucleus.texturing.TexParameter;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureType;
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
    /**
     * The current concatenated modelview matrix
     */
    protected float[] mvMatrix = Matrix.createMatrix();
    /**
     * Reference to the current modelmatrix, each Node has its own Matrix that is referenced.
     */
    protected float[] modelMatrix;

    /**
     * The view matrix
     */
    protected float[] viewMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
    /**
     * The current projection matrix
     */
    protected float[] projectionMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);

    protected GLES20Wrapper gles;
    protected ImageFactory imageFactory;
    protected MatrixEngine matrixEngine;
    private Set<RenderContextListener> contextListeners = new HashSet<RenderContextListener>();
    private Set<FrameListener> frameListeners = new HashSet<BaseRenderer.FrameListener>();

    private FrameSampler timeKeeper = FrameSampler.getInstance();
    private float deltaTime;

    protected Window window = Window.getInstance();

    protected RendererInfo rendererInfo;
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
    protected BaseRenderer(GLES20Wrapper gles, ImageFactory imageFactory, MatrixEngine matrixEngine) {
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
        rendererInfo = new RendererInfo(gles);
        gles.glLineWidth(1f);
    }

    @Override
    public float beginFrame() {
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
                gles.glDepthMask(false);
            }
        }
        if ((flags & RenderState.CHANGE_FLAG_MULTISAMPLE) != 0) {
            if (rendererInfo.hasExtensionSupport(GLESWrapper.GLES_EXTENSIONS.MULTISAMPLE_EXT.name())) {
                if (surfaceConfig != null && surfaceConfig.getSamples() > 1 && state.isMultisampling()) {
                    gles.glEnable(GLES_EXTENSIONS.MULTISAMPLE_EXT.value);
                } else {
                    gles.glDisable(GLES_EXTENSIONS.MULTISAMPLE_EXT.value);
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
        State state = node.getState();
        if (state == null || state == State.ON || state == State.RENDER) {
            if (node.getType().equals(NodeTypes.renderpass.name())) {
                // Renderpass node - set renderstate
                internalRender((RenderPass) node);
            } else {
                internalRender(node);
            }
        }
    }

    private void internalRender(Node node) throws GLException {
        // Check for AttributeUpdate producer.
        Producer producer = node.getAttributeProducer();
        if (producer != null) {
            producer.updateAttributeData();
        }
        float[] nodeMatrix = node.concatModelMatrix(this.modelMatrix);
        // Fetch projection just before render
        float[] projection = node.getProjection();
        if (projection != null) {
            pushMatrix(this.projection, this.projectionMatrix);
            this.projectionMatrix = projection;
        }
        Matrix.mul4(nodeMatrix, viewMatrix, mvMatrix);
        // Matrix.mul4(mvMatrix, projectionMatrix);
        renderMeshes(node.getMeshes(), mvMatrix, projectionMatrix);
        this.modelMatrix = nodeMatrix;
        for (Node n : node.getChildren()) {
            pushMatrix(matrixStack, this.modelMatrix);
            render(n);
            this.modelMatrix = popMatrix(matrixStack);
        }
        if (projection != null) {
            this.projectionMatrix = popMatrix(this.projection);
        }
        node.getRootNode().addRenderedNode(node);

    }

    private void setupRenderTarget(RenderTarget target) throws GLException {
        if (target.getBufferObjectName() == Constants.NO_VALUE) {
            setupBuffers(target);
        }
        switch (target.getTarget()) {
            case FRAMEBUFFER:
                break;
            case TEXTURE:
                setupTextureRenderTarget(target);
                break;
            default:
                throw new IllegalArgumentException("Not implemented");
        }
    }

    private void setupTextureRenderTarget(RenderTarget target) throws GLException {
        gles.glActiveTexture(GLES20.GL_TEXTURE0);
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, target.getTargetName());
        GLUtils.handleError(gles, "glBindTexture");
        TextureParameter texParams = new TextureParameter(
                new TexParameter[] { TexParameter.NEAREST, TexParameter.NEAREST, TexParameter.CLAMP,
                        TexParameter.CLAMP });
        gles.uploadTexParameters(texParams);
        gles.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, target.getBufferObjectName());
        gles.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                target.getTargetName(), 0);
        GLUtils.handleError(gles, "glFramebufferTexture");
        gles.glDisable(GLES20.GL_DEPTH_TEST);
        GLUtils.handleError(gles, "glFramebufferTexture");
        if (gles.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalArgumentException("Could not setup render target");
        }
    }

    /**
     * Creates and initializes the buffers needed for the rendertarget
     * @param target
     */
    private void setupBuffers(RenderTarget target) throws GLException {
        if (target.getBufferObjectName() == Constants.NO_VALUE) {
            target.setFramebufferName(createFramebuffer());
        }
        for (AttachementData ad : target.getAttachements()) {
            switch (ad.getAttachement()) {
                case COLOR:
                    setupColorBuffer(ad, target.getTarget());
                    break;
                case DEPTH:
                    setupDepthBuffer(ad, target.getTarget());
                    break;
                case STENCIL:
                    setupStencilBuffer(ad, target.getTarget());
                    break;
            }
        }
    }

    /**
     * Initializes the buffer needed for the color buffer attachement
     * Currently only supports render to texture
     * @param attachementData
     * @param target
     * @throws GLException
     */
    private void setupColorBuffer(AttachementData attachementData, Target target) throws GLException {
        attachementData.setBufferName(createTexture(attachementData));
    }
    private void setupDepthBuffer(AttachementData attachementData, Target target) throws GLException {
        attachementData.setBufferName(createTexture(attachementData));

    }
    private void setupStencilBuffer(AttachementData attachementData, Target target) throws GLException {
        attachementData.setBufferName(createTexture(attachementData));
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
     * @param attachementData
     * @return Texture object name
     * @throws GLException
     */
    private int createTexture(AttachementData attachementData) throws GLException {
        int[] size = attachementData.getSize();
        int[] name = new int[1];
        gles.glGenTextures(name);
        gles.glActiveTexture(GLES20.GL_TEXTURE0);
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, name[0]);
        ImageFormat format = ImageFormat.valueOf(attachementData.getFormat());
        Texture2D.Format texFormat = TextureUtils.getFormat(format);
        Texture2D.Type texType = TextureUtils.getType(format);
        gles.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, texFormat.format, size[0], size[1], 0, texFormat.format,
                texType.type, null);
        GLUtils.handleError(gles, "glTexImage2D");
        return name[0];
    }

    private void internalRender(RenderPass node) throws GLException {
        setupRenderTarget(node.getTarget());

        RenderState state = node.getRenderState();
        if (state != null) {
            if (state.getChangeFlag() != RenderState.CHANGE_FLAG_NONE) {
                setRenderState(state);
                state.setChangeFlag(RenderState.CHANGE_FLAG_NONE);
            }
            int clearFunc = state.getClearFunction();
            if (clearFunc != GLES20.GL_NONE) {
                gles.glClear(clearFunc);
            }
        }
        internalRender((Node) node);
    }

    protected void renderMeshes(ArrayList<Mesh> meshes, float[] mvMatrix, float[] projectionMatrix) throws GLException {
        for (Mesh mesh : meshes) {
            renderMesh(mesh, mvMatrix, projectionMatrix);
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
     * @param mvMatrix accumulated modelview matrix for this mesh, this will be sent to uniform.
     * @param projectionMatrix The projection matrix, depending on shader this is either concatenated
     * with modelview set to unifom.
     * @throws GLException If there is an error in GL while drawing this mesh.
     */
    protected void renderMesh(Mesh mesh, float[] mvMatrix, float[] projectionMatrix) throws GLException {
        Consumer updater = mesh.getAttributeConsumer();
        if (updater != null) {
            updater.updateAttributeData();
        }
        Material material = mesh.getMaterial();
        ShaderProgram program = material.getProgram();
        AttributeBuffer vertices = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        ElementBuffer indices = mesh.getElementBuffer();
        gles.glUseProgram(program.getProgram());
        GLUtils.handleError(gles, "glUseProgram " + program.getProgram());

        Texture2D texture = mesh.getTexture(Texture2D.TEXTURE_0);
        if (texture != null && texture.textureType != TextureType.Untextured) {
            int textureID = texture.getName();
            gles.glActiveTexture(GLES20.GL_TEXTURE0);
            gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
            gles.uploadTexParameters(texture.getTexParams());
        }
        material.setBlendModeSeparate(gles);
        program.bindAttributes(gles, mesh);
        program.bindUniforms(gles, mvMatrix, projectionMatrix, mesh);

        if (indices == null) {
            gles.glDrawArrays(mesh.getMode().mode, 0, vertices.getVerticeCount());
            timeKeeper.addDrawArrays(vertices.getVerticeCount());
        } else {
            if (indices.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
                gles.glDrawElements(mesh.getMode().mode, indices.getCount(), indices.getType().type, 0);
            } else {
                gles.glDrawElements(mesh.getMode().mode, indices.getCount(), indices.getType().type,
                        indices.getBuffer().position(0));
            }
            timeKeeper.addDrawElements(vertices.getVerticeCount(), indices.getCount());
        }
        GLUtils.handleError(gles, "glDrawArrays ");
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
        List<Node> scene = root.getScene();
        if (scene != null) {
            for (Node node : scene) {
                render(node);
            }
        }
    }

    @Override
    public void processFrame() {
        for (FrameListener listener : frameListeners) {
            listener.processFrame(deltaTime);
        }
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
        gles.glViewport(x, y, width, height);
        Window.getInstance().setSize(width, height);
    }

    @Override
    public void setProjection(float[] matrix, int index) {
        System.arraycopy(matrix, index, projectionMatrix, 0, 16);
    }

    @Override
    public float[] getProjection() {
        return projectionMatrix;
    }

    @Override
    public SurfaceConfiguration getSurfaceConfiguration() {
        return surfaceConfig;
    }

    @Override
    public RendererInfo getInfo() {
        return rendererInfo;
    }

}
