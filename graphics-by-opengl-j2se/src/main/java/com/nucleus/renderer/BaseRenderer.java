package com.nucleus.renderer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.profiling.FrameSampler;
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

    private FrameSampler timeKeeper = new FrameSampler(30);
    private float deltaTime;

    protected Window window = Window.getInstance();

    protected GLInfo glInfo;
    private Node scene;
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

    @Override
    public void GLContextCreated(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(RenderContextListener.INVALID_CONTEXT_DIMENSION);
        }
        window.setDimension(width, height);
        for (RenderContextListener listener : contextListeners) {
            listener.contextCreated(width, height);
        }
    }

    @Override
    public void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        glInfo = new GLInfo(gles);
    }

    @Override
    public float beginFrame() {
        for (FrameListener listener : frameListeners) {
            listener.updateGLData();
        }
        deltaTime = timeKeeper.update();
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
            gles.glClearColor(0.6f, 0.5f, 0.4f, 1.0f);
            gles.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            gles.glDisable(GLES20.GL_DEPTH_TEST);
            gles.glDisable(GLES20.GL_CULL_FACE);
            GLUtils.handleError(gles, "Error");

        } catch (GLException e) {
            throw new RuntimeException(e);
        }
        return deltaTime;
    }

    @Override
    public void endFrame() {
    }

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
        VertexBuffer vertices = mesh.getVerticeBuffer(BufferIndex.VERTICES);
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

    @Override
    public ViewFrustum getViewFrustum() {
        return viewFrustum;
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

    @Override
    public void setScene(Node scene) {
        this.scene = scene;
    }

    @Override
    public void renderScene() throws GLException {
        render(scene);
    }

    @Override
    public Node getScene() {
        return scene;
    }

    @Override
    public void processFrame() {
        for (FrameListener listener : frameListeners) {
            listener.processFrame(deltaTime);
        }
    }

}
