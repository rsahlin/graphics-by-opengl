package com.nucleus.renderer;

import java.nio.Buffer;
import java.util.ArrayDeque;
import java.util.ArrayList;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.camera.ViewPort;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.AttributeUpdater.Producer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES_EXTENSIONS;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeState;
import com.nucleus.scene.NodeType;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.ViewNode;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.ImageFactory;
import com.nucleus.texturing.Texture2D;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Transform;

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

    private float[] tempMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);

    protected GLES20Wrapper gles;
    protected RenderSettings renderSettings = new RenderSettings();
    protected ImageFactory imageFactory;
    protected MatrixEngine matrixEngine;
    private ArrayList<RenderContextListener> contextListeners = new ArrayList<RenderContextListener>();
    private ArrayList<FrameListener> frameListeners = new ArrayList<BaseRenderer.FrameListener>();

    private FrameSampler timeKeeper = new FrameSampler(30);
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
    public void GLContextCreated(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(RenderContextListener.INVALID_CONTEXT_DIMENSION);
        }
        window.setSize(width, height);
        renderSettings.setViewPort(0, 0, width, height);

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
    }


    @Override
    public float beginFrame() {
        deltaTime = timeKeeper.update();
        if (timeKeeper.getSampleDuration() > 3) {
            System.out.println(BASE_RENDERER_TAG + ": " + timeKeeper.sampleFPS());
        }
        try {
            if (renderSettings.getChangeFlag() != RenderSettings.CHANGE_FLAG_NONE) {
                setRenderSetting(renderSettings);
                renderSettings.setChangeFlag(RenderSettings.CHANGE_FLAG_NONE);
            }
        } catch (GLException e) {
            throw new RuntimeException(e);
        }
        int clearFunc = renderSettings.getClearFunction();
        if (clearFunc != GLES20.GL_NONE) {
            gles.glClear(clearFunc);
        }
        for (FrameListener listener : frameListeners) {
            listener.updateGLData();
        }
        // matrixEngine.setProjectionMatrix(viewFrustum);
        // mvpMatrix = getViewFrustum().getProjectionMatrix();
        this.modelMatrix = null;

        return deltaTime;
    }

    /**
     * Internal method to apply the rendersettings.
     * 
     * @param setting
     * @throws GLException
     */
    private void setRenderSetting(RenderSettings setting) throws GLException {

        int flags = setting.getChangeFlag();
        if ((flags & RenderSettings.CHANGE_FLAG_CLEARCOLOR) != 0) {
            float[] clear = setting.getClearColor();
            gles.glClearColor(clear[0], clear[1], clear[2], clear[3]);
        }
        if ((flags & RenderSettings.CHANGE_FLAG_CULLFACE) != 0) {
            // Set GL values.
            if (setting.getCullFace() != GLES20.GL_NONE) {
                gles.glEnable(GLES20.GL_CULL_FACE);
                gles.glCullFace(setting.getCullFace());
            } else {
                gles.glDisable(GLES20.GL_CULL_FACE);
            }
        }
        if ((flags & RenderSettings.CHANGE_FLAG_DEPTH) != 0) {
            if (setting.getDepthFunc() != GLES20.GL_NONE) {
                gles.glEnable(GLES20.GL_DEPTH_TEST);
                gles.glDepthFunc(setting.getDepthFunc());
                gles.glDepthMask(true);
                gles.glClearDepthf(setting.getClearDepth());
                gles.glDepthRangef(setting.getDepthRangeNear(), setting.getDepthRangeFar());
            } else {
                gles.glDisable(GLES20.GL_DEPTH_TEST);
                gles.glDepthMask(false);
            }
        }
        if ((flags & RenderSettings.CHANGE_FLAG_MULTISAMPLE) != 0) {
            if (rendererInfo.hasExtensionSupport(GLESWrapper.GLES_EXTENSIONS.MULTISAMPLE_EXT.name())) {
                if (surfaceConfig != null && surfaceConfig.getSamples() > 1 && setting.isMultisampling()) {
                    gles.glEnable(GLES_EXTENSIONS.MULTISAMPLE_EXT.value);
                } else {
                    gles.glDisable(GLES_EXTENSIONS.MULTISAMPLE_EXT.value);
                }
            }
        }
        if ((flags & RenderSettings.CHANGE_FLAG_VIEWPORT) != 0) {
            int[] view = renderSettings.getViewPort();
            gles.glViewport(view[ViewPort.VIEWPORT_X], view[ViewPort.VIEWPORT_Y],
                    view[ViewPort.VIEWPORT_WIDTH], view[ViewPort.VIEWPORT_HEIGHT]);
            
        }
        GLUtils.handleError(gles, "setRenderSettings ");

    }

    @Override
    public void endFrame() {
    }

    @Override
    public void render(Node node) throws GLException {
        NodeState state = node.getState();
        if (state == null || state == NodeState.on || state == NodeState.render) {
            if (NodeType.viewnode.name().equals(node.getType())) {
                Transform view = ((ViewNode) node).getView();
                if (view != null) {
                    setView(view.getMatrix(), 0);
                }
            }
            // Check for AttributeUpdate producer.
            Producer producer = node.getAttributeProducer();
            if (producer != null) {
                producer.updateAttributeData();
            }
            float[] nodeMatrix = node.getModelMatrix();
            float[] modelMatrix = null;
            if (node.getTransform() != null) {
                modelMatrix = node.getTransform().getMatrix();
            } else {
                modelMatrix = tempMatrix;
                Matrix.setIdentity(modelMatrix, 0);
            }
            if (this.modelMatrix == null) {
                System.arraycopy(modelMatrix, 0, nodeMatrix, 0, 16);
            } else {
                Matrix.mul4(this.modelMatrix, modelMatrix, nodeMatrix);
            }
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
        }
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
        VertexBuffer vertices = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        ElementBuffer indices = mesh.getElementBuffer();
        gles.glUseProgram(program.getProgram());
        GLUtils.handleError(gles, "glUseProgram ");

        Texture2D texture = mesh.getTexture(Texture2D.TEXTURE_0);
        if (texture != null) {
            int textureID = texture.getName();
            gles.glActiveTexture(GLES20.GL_TEXTURE0);
            gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
            texture.uploadTexParameters(gles);
        }
        material.setBlendModeSeparate(gles);
        program.bindAttributes(gles, mesh);
        program.bindUniforms(gles, mvMatrix, projectionMatrix, mesh);

        if (indices == null) {
            gles.glDrawArrays(mesh.getMode(), 0, vertices.getVerticeCount());
            timeKeeper.addDrawArrays(vertices.getVerticeCount());
        } else {
            if (indices.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
                gles.glDrawElements(indices.getMode().mode, indices.getCount(), indices.getType().type, 0);
            } else {
                gles.glDrawElements(indices.getMode().mode, indices.getCount(), indices.getType().type,
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
        if (root.getScene() != null) {
            render(root.getScene());
        }
    }

    @Override
    public void processFrame() {
        for (FrameListener listener : frameListeners) {
            listener.processFrame(deltaTime);
        }
    }

    @Override
    public void genBuffers(int count, int[] names, int offset) {
        gles.glGenBuffers(count, names, offset);
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
    public RenderSettings getRenderSettings() {
        return renderSettings;
    }

    @Override
    public FrameSampler getFrameSampler() {
        return timeKeeper;
    }

    @Override
    public void resizeWindow(int x, int y, int width, int height) {
        renderSettings.setViewPort(x, y, width, height);
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
    public void setView(float[] matrix, int index) {
        System.arraycopy(matrix, index, viewMatrix, 0, 16);
    }

    @Override
    public float[] getView() {
        return viewMatrix;
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
