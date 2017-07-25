package com.nucleus;

import com.nucleus.assets.AssetManager;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.component.J2SELogicProcessor;
import com.nucleus.component.LogicProcessorRunnable;
import com.nucleus.convolution.ConvolutionProgram;
import com.nucleus.event.EventManager.EventHandler;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.ExternalReference;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.core.PointerInputProcessor;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.opengl.GLException;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.FrameListener;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.renderer.RenderSettings;
import com.nucleus.renderer.SurfaceConfiguration;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.BaseRootNode;
import com.nucleus.scene.J2SENodeInputListener;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node.MeshType;
import com.nucleus.scene.NodeController;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.ViewController;
import com.nucleus.texturing.TexParameter;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureParameter;

/**
 * Base application, use this to get the objects needed to start and run an application.
 * Used by JOGL and Android implementations to share objects that are platform agnostic.
 * 
 * @author Richard Sahlin
 *
 */
public class CoreApp {

    private final static String NOT_CALLED_CREATECONTEXT = "Must call contextCreated() before rendering.";

    /**
     * Interface for the core app to create the objects needed.
     * Used by the internal implementation and not by clients.
     * 
     * @author Richard Sahlin
     *
     */
    public interface CoreAppStarter {
        /**
         * Create the core renderer and windows, this method is called before GL context is available.
         * Note, this method will only create the underlying renderer of the correct version, it is not guaranteed
         * that the GL context is created - do not perform any rendering until the {@link #createCoreApp(int, int)}
         * method
         * is called.
         * 
         * @param version Version of GLES to use.
         */
        public void createCoreWindows(Renderers version);
        
        /**
         * Create the {@link CoreApp} implementation for the platform, the renderer is now created and has a valid GL context.
         * Do one time setup - for instance displaying splash and create, but do not initialize the {@link ClientApplication}
         * 
         * @param width The width of the display window
         * @param height The height of the display window
         */
        public void createCoreApp(int width, int height);
    }

    /**
     * Implement this interface in client applications that are using {@link CoreApp} This interface is intended for the
     * J2SE (platform agnostic) version of projects.
     * 
     * 
     * @author Richard Sahlin
     *
     */
    public interface ClientApplication {

        /**
         * Initializes the client application with the {@link CoreApp} Implementations shall do necessary init in this
         * method.
         * 
         * @param coreApp CoreApp that is fully initialized and context has already been created
         * @throws IllegalArgumentException If coreApp is null
         */
        public void init(CoreApp coreApp);

    }

    /**
     * The renderer
     */
    protected NucleusRenderer renderer;
    /**
     * The implementation of main application
     */
    protected ClientApplication clientApp;
    /**
     * The scene rootnode
     */
    protected RootNode rootNode;

    /**
     * Touch and pointer input
     */
    protected PointerInputProcessor inputProcessor = new PointerInputProcessor();

    Thread runnableThread;
    LogicProcessorRunnable logicRunnable;

    /**
     * Set to true when {@link #contextCreated(int, int)} is called
     */
    private volatile boolean hasCalledCreated = false;

    /**
     * Creates a new Core application with the specified renderer.
     * Call {@link #drawFrame()} to produce frames and call attached {@link FrameListener}
     * 
     * @param renderer Initialized renderer
     * @param clientApp The client main app implementation
     * @throws IllegalArgumentException If renderer has not been initialized or is null, or if clientApp is null
     */
    public CoreApp(NucleusRenderer renderer, ClientApplication clientApp) {
    	if (renderer == null) {
    		throw new IllegalArgumentException("Renderer is null");
    	}
    	if (clientApp == null) {
    		throw new IllegalArgumentException("ClientApplication is null");
    	}
    	if (!renderer.isInitialized()) {
    		throw new IllegalArgumentException("Renderer has not been initialized");
    	}
        this.renderer = renderer;
        this.clientApp = clientApp;
        logicRunnable = new LogicProcessorRunnable(renderer, new J2SELogicProcessor());
        if (Runtime.getRuntime().availableProcessors() > 1) {
            System.out.println("Started extra process for logic processing, number of processors: "
                    + Runtime.getRuntime().availableProcessors());
            runnableThread = new Thread(logicRunnable);
        } else {
            System.out.println("Running everything on one thread.");
        }

    }

    /**
     * Returns the renderer, this should generally not be used.
     * 
     * @return
     */
    public NucleusRenderer getRenderer() {
        return renderer;
    }

    /**
     * Returns the pointer input processor, this can be used to listen to low level MMI (pointer input) events.
     * 
     * @return
     */
    public PointerInputProcessor getInputProcessor() {
        return inputProcessor;
    }

    /**
     * Call this to signal that EGL context was created, if this is the first time context is created then display the
     * splash screen.
     * Will initialize the renderer and call {@link NucleusRenderer# (int, int)}
     * Must be called before {@link #drawFrame()} is called.
     * 
     * @param width Width of gl surface
     * @param height Height of gl surface
     */
    public void contextCreated(int width, int height) {
        hasCalledCreated = true;
        if (!getRenderer().isInitialized()) {
            getRenderer().init(new SurfaceConfiguration(), width, height);
            try {
                // The caller shall make sure that buffers are swapped so that the result is visible
                displaySplash();
            } catch (GLException e) {
                throw new RuntimeException(e);
            }
        }
        clientApp.init(this);
        renderer.contextCreated(width, height);
    }

    /**
     * Called when the rendering surface is destroyed and the EGL context is lost.
     * This just means that the render surface and EGL context is not available.
     * The app may just have been switched to background.
     * This may be followed by a call to contextCreated() in which case only textures needs to be
     * re-created.
     */
    public void surfaceLost() {
        SimpleLogger.d(getClass(), "surfaceLost()");
    }

    /**
     * Release all resources used by this application.
     * Call destroy on AssetManager
     */
    public void destroy() {
        AssetManager.getInstance().destroy(renderer);
        rootNode.destroy(renderer);
    }

    /**
     * Main loop, call this method to produce one frame.
     * This method MUST be called from a thread that can access GL.
     * The normal case is to call it from window/surface that has onDraw/display callbacks.
     */
    public void drawFrame() {
        if (!hasCalledCreated) {
            throw new IllegalArgumentException(NOT_CALLED_CREATECONTEXT);
        }
        try {
            if (runnableThread != null) {
                if (!runnableThread.isAlive()) {
                    runnableThread.start();
                } else {
                    synchronized (logicRunnable) {
                        logicRunnable.notify();
                    }
                }
            } else {
                logicRunnable.process(FrameSampler.getInstance().getDelta());
            }
            renderer.beginFrame();
            if (rootNode != null) {
                renderer.render(rootNode);
            }
        } catch (GLException e) {
            throw new RuntimeException(e);
        }
        renderer.endFrame();
        if (rootNode != null) {
            rootNode.swapNodeList();
        }

    }

    /**
     * Sets the scene rootnode, this will update the root node in the logic runnable {@linkplain LogicProcessorRunnable}
     * A {@linkplain NodeController} will be created for the node and used as {@linkplain EventHandler}
     * 
     * @param node
     */
    public void setRootNode(RootNode node) {
        FrameSampler.getInstance().logTag(FrameSampler.SET_ROOT_NODE);
        this.rootNode = node;
        logicRunnable.setRootNode(node);
        ViewController vc = new ViewController();
        vc.registerEventHandler(null);
        NodeController nc = new NodeController(node);
        nc.registerEventHandler(null);

    }

    /**
     * Adds pointer input callback {@linkplain MMIEventListener} to the scene, after this call the Node tree will get
     * callbacks on pointer input
     * 
     * @param root
     */
    public void addPointerInput(RootNode root) {
        inputProcessor.addMMIListener(new J2SENodeInputListener(root));
    }

    public void displaySplash() throws GLException {
        FrameSampler.getInstance().logTag(FrameSampler.DISPLAY_SPLASH);
        RenderSettings rs = renderer.getRenderSettings();
        rs.setCullFace(GLES20.GL_NONE);
        rs.setDepthFunc(GLES20.GL_NONE);
        Mesh mesh = new Mesh();
        ConvolutionProgram c = new ConvolutionProgram();
        c.createProgram(renderer.getGLES());
        BaseRootNode root = new BaseRootNode();
        LayerNode node = new LayerNode();
        node.setRootNode(root);
        node.setLayer(Layer.SCENE);
        ViewFrustum vf = new ViewFrustum();
        vf.setOrthoProjection(-0.5f, 0.5f, 0.5f, -0.5f, 0, 10);
        node.setViewFrustum(vf);
        TextureParameter texParam = new TextureParameter();
        texParam.setValues(new TexParameter[] { TexParameter.NEAREST, TexParameter.NEAREST, TexParameter.CLAMP,
                TexParameter.CLAMP });
        Texture2D tex = TextureFactory.createTexture(renderer.getGLES(), renderer.getImageFactory(),
                "texture", new ExternalReference("assets/splash.png"), RESOLUTION.HD, texParam, 1);
        float[] kernel = new float[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };
        // Convolution.normalize(kernel, kernel, false, 1);
        c.buildMesh(mesh, tex, 0.2f, 0.2f, 0, kernel);
        node.addMesh(mesh, MeshType.MAIN);
        root.setScene(node);
        renderer.beginFrame();
        renderer.render(root);
        renderer.endFrame();
    }
    
    /**
     * Util method to create the coreapp and display splash. Caller must swapp buffer for splash to be visible.
     * 
     * @param width
     * @param height
     * @param renderer
     * @param clientClass
     * @return Instance of {@link CoreApp} with the specified clientclass {@link ClientApplication}
     */
    public static CoreApp createCoreApp(int width, int height, NucleusRenderer renderer, Class<?> clientClass) {
    	renderer.init(new SurfaceConfiguration(), width, height);
    	try {
            CoreApp coreApp = new CoreApp(renderer, (ClientApplication) clientClass.newInstance());
            try {
                coreApp.displaySplash();
            } catch (GLException e) {
            	throw new RuntimeException(e);
            }
            return coreApp;
    	} catch (IllegalAccessException |InstantiationException e) {
    		throw new RuntimeException(e);
    	}
    }
    
}
