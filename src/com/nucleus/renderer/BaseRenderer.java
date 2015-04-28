package com.nucleus.renderer;

import java.util.ArrayList;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.TimeKeeper;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.resource.ResourceBias;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.Node;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Image;
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
public class BaseRenderer {

    public interface RenderContextListener {
        /**
         * Called when the rendering context is create and ready to be used. Can also be called if
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
         * This method will be called by the beginFrame() method, ie before rendering takes place.
         * 
         * @param deltaTime
         */
        public void processFrame(float deltaTime);
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

    protected final static String BASE_RENDERER_TAG = "BaseRenderer";

    public final static String NULL_GLESWRAPPER_ERROR = "GLES wrapper is null";
    public final static String NULL_IMAGEFACTORY_ERROR = "ImageFactory is null";
    public final static String NULL_MATRIXENGINE_ERROR = "MatrixEngine is null";

    protected ViewFrustum viewFrustum = new ViewFrustum();

    protected GLES20Wrapper gles;
    protected ImageFactory imageFactory;
    protected MatrixEngine matrixEngine;
    private ArrayList<RenderContextListener> contextListeners = new ArrayList<RenderContextListener>();
    private ArrayList<FrameListener> frameListeners = new ArrayList<BaseRenderer.FrameListener>();

    private TimeKeeper timeKeeper = new TimeKeeper(30);

    /**
     * Implementations shall set the width of the target display
     */
    protected int width;
    /**
     * Implementations shall set the height of the target display
     */
    protected int height;

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
    public void GLContextCreated(int width, int height) {
        this.width = width;
        this.height = height;
        for (RenderContextListener listener : contextListeners) {
            listener.contextCreated(width, height);
        }
    }

    /**
     * Call this first time when the context is created, before calling GLContextCreated()
     * Initialize parameters that do not need to be updated when context is re-created.
     */
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
    public float beginFrame() {
        float deltaTime = timeKeeper.update();
        if (timeKeeper.getSampleDuration() > 3) {
            System.out.println(BASE_RENDERER_TAG + ": Average FPS: " + timeKeeper.sampleFPS());
        }
        for (FrameListener listener : frameListeners) {
            listener.processFrame(deltaTime);
        }
        return deltaTime;
    }

    /**
     * Signals the end of a frame - rendering is considered to be finished and implementations should call
     * EGL.swapBuffers() if needed
     * This shall be called by the thread driving rendering.
     */
    public void endFrame() {

    }

    /**
     * The main render method, all drawing shall take place here.
     * Call this method when a new frame shall be produced by GL
     * Note that implementations shall not swap buffer in this method, this method shall ONLY render to the currently
     * attached framebuffer.
     */
    public void render() {
        // For now always set the viewport
        // TODO: Add dirty flag in viewport and only set when updated.
        int[] viewport = viewFrustum.getViewPort();
        gles.glViewport(viewport[ViewFrustum.VIEWPORT_X], viewport[ViewFrustum.VIEWPORT_Y],
                viewport[ViewFrustum.VIEWPORT_WIDTH], viewport[ViewFrustum.VIEWPORT_HEIGHT]);
        matrixEngine.setProjectionMatrix(viewFrustum);

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

    }

    /**
     * Renders the node
     * 
     * @param node The node to be rendered
     * @throws GLException If there is an error in GL while drawing this node.
     */
    public void render(Node node) throws GLException {
        float[] viewMatrix = getViewFrustum().getProjectionMatrix();
        float[] modelMatrix = node.getTransform().getMatrix();
        float[] mvp = new float[16];
        Matrix.mul4(viewMatrix, modelMatrix, mvp);
        renderMesh(node.getMesh(), mvp);
    }

    /**
     * Renders one mesh, material is used to fetch program and set attributes/uniforms.
     * If texture exists in mesh it is made active and used.
     * If mesh contains an index buffer it is used and glDrawElements is called, otherwise
     * drawArrays is called.
     * 
     * @param mesh The mesh to be rendere.
     * @param mvpMatrix accumulated matrix for this mesh, this will be sent to uniform.
     * @throws GLException If there is an error in GL while drawing this mesh.
     */
    protected void renderMesh(Mesh mesh, float[] mvpMatrix) throws GLException {

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
            texture.setTexParameters(gles);
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
    public ViewFrustum getViewFrustum() {
        return viewFrustum;
    }

    /**
     * Returns true if this renderer has been initialized by calling init() when
     * the context is created.
     * 
     * @return
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns the width in pixels of the display
     * 
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height in pixels of the display
     * 
     * @return
     */
    public int getHeight() {
        return height;
    }

    public void createProgram(ShaderProgram program) {
        program.createProgram(gles);
    }

    /**
     * Creates a texture for the specified image, the texture will be scaled according to target resolution (for the
     * image) and current height of screen.
     * 
     * @param imageName
     * @param levels Number of MIP-MAP levels to create, will use scaled version of image.
     * @param targetResolution
     * @return
     */
    public Texture2D createTexture(String imageName, int levels, RESOLUTION targetResolution) {
        int[] textures = new int[1];
        gles.glGenTextures(1, textures, 0);

        int textureID = textures[0];

        Image[] textureImg = TextureUtils.loadTextureMIPMAP(imageFactory, imageName,
                ResourceBias.getScaleFactorLandscape(width, height, targetResolution.lines), levels);

        try {
            uploadTextures(GLES20.GL_TEXTURE0, textureID, textureImg);
        } catch (GLException e) {
            throw new IllegalArgumentException(e);
        }
        return new Texture2D(textureID, textureImg[0].getWidth(), textureImg[0].getHeight());

    }

    /**
     * Sets the active texture, binds texName and calls glTexImage2D on the images in the array where
     * mip-map level will be same as the image index.
     * 
     * @param texture Texture unit number (active texture)
     * @param texName Name of texture object
     * @param textureImages Array with one or more images to send to GL. If more than
     * one image is specified then multiple mip-map levels will be set.
     * Level 0 shall be at index 0
     * @throws GLException If there is an error uploading the textures.
     */
    public void uploadTextures(int texture, int texName, Image[] textureImages) throws GLException {
        gles.glActiveTexture(texture);
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, texName);
        int level = 0;
        for (Image textureImg : textureImages) {
            if (textureImg != null) {
                gles.glTexImage2D(GLES20.GL_TEXTURE_2D, level, textureImg.getFormat().getFormat(),
                        textureImg.getWidth(),
                        textureImg
                                .getHeight(), 0, textureImg.getFormat().getFormat(), GLES20.GL_UNSIGNED_BYTE,
                        textureImg
                                .getBuffer()
                                .position(0));
                GLUtils.handleError(gles, "texImage2D");
            }
            level++;
        }

    }

    /**
     * Adds a listener for render context created, if listener is already added nothing is done.
     * 
     * @param listener Listener to get callback when render context is created.
     */
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
    public void addFrameListener(FrameListener listener) {
        if (frameListeners.contains(listener)) {
            return;
        }
        frameListeners.add(listener);
    }

}
