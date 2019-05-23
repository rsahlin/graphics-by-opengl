package com.nucleus;

import com.nucleus.assets.AssetManager;
import com.nucleus.common.Type;
import com.nucleus.component.ComponentProcessorRunnable;
import com.nucleus.component.J2SEComponentProcessor;
import com.nucleus.event.EventManager;
import com.nucleus.event.EventManager.EventHandler;
import com.nucleus.mmi.core.CoreInput;
import com.nucleus.opengl.GLException;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.profiling.FrameSampler.Sample;
import com.nucleus.profiling.FrameSampler.Samples;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.FrameRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;
import com.nucleus.renderer.Window;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.J2SENodeInputListener;
import com.nucleus.scene.NavigationController;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeController;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.RootNodeBuilder;
import com.nucleus.scene.ViewController;
import com.nucleus.system.ComponentHandler;
import com.nucleus.ui.UIElementInput;

/**
 * The platform agnostic application, this is the main app for J2SE platform independent code.
 * Base application, use this to get the objects needed to start and run an application.
 * Used by JOGL and Android implementations to share objects that are platform agnostic.
 * 
 * @author Richard Sahlin
 *
 */
public class CoreApp implements FrameRenderer {

    private static final String NOT_CALLED_CREATECONTEXT = "Must call contextCreated() before rendering.";
    private static final String SPLASH_FILENAME = "assets/splash.png";

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
         * @param version Version of renderer to use, GLES/VULKAN
         */
        public void createCoreWindows(Renderers version);

        /**
         * Create the {@link CoreApp} implementation for the platform, the renderer is now created and has a valid GL
         * context.
         * Do one time setup - for instance displaying splash and create, but do not initialize the
         * {@link ClientApplication}
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

        /**
         * Called before a new frame is renderer, after this call the scene will be processed to produce the next frame.
         * Can be used to update objects that needs to be synchronized to a pre-frame behavior.
         * 
         * @param deltaTime Time, in seconds, that it took to create last frame
         */
        public void beginFrame(float deltaTime);

        /**
         * Called after a new frame has been rendered. Make no assumptions of what is currently on screen as the swap
         * behavior is unknown.
         * All draw calls have been sent to graphics layer, though they may not have finished processing.
         * Can be used to update objects that needs to be synchronized to a post-frame behavior.
         * 
         * @param deltaTime Time, in seconds, that it took to create last frame
         */
        public void endFrame(float deltaTime);

        /**
         * Returns the name of the application
         * 
         * @return
         */
        public String getAppName();

        /**
         * Returns the version
         * 
         * @return
         */
        public String getVersion();

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
     * Class to instantiate for ClientApplication
     */
    protected static Class<?> clientClass;
    /**
     * The scene rootnode
     */
    protected RootNode rootNode;

    ComponentProcessorRunnable componentRunnable;

    /**
     * Set to true when {@link #contextCreated(int, int)} is called
     */
    private volatile boolean hasCalledCreated = false;

    /**
     * Set to true to release resources after {@link #drawFrame()} has finished.
     */
    private volatile boolean destroy = false;

    /**
     * Creates a new Core application with the specified renderer.
     * Use {@link FrameRenderer} to produce frames and keep track of context events.
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

        componentRunnable = new ComponentProcessorRunnable(renderer, new J2SEComponentProcessor(), false);

    }

    /**
     * Returns the renderer, this should generally not be used.
     * 
     * @return
     */
    public NucleusRenderer getRenderer() {
        return renderer;
    }

    @Override
    public void contextCreated(int width, int height) {
        hasCalledCreated = true;
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
    @Override
    public void surfaceLost() {
        SimpleLogger.d(getClass(), "surfaceLost()");
    }

    /**
     * Release all resources used by this application.
     * Only call this when there is no render in progress and from thread that can acess GL.
     * Call destroy on AssetManager, set renderer to null and exit threads.
     * DO NOT USE this class after calling this method.
     */
    protected void destroy() {
        AssetManager.getInstance().destroy(renderer);
        rootNode.destroy(renderer);
        renderer = null;
        componentRunnable.destroy();
    }

    /**
     * Sets the destroy flag to true, this will release all GL resources after {@link #drawFrame()} has finished.
     */
    public void setDestroyFlag() {
        destroy = true;
    }

    /**
     * Main loop, call this method to produce one frame.
     * This method MUST be called from a thread that can access GL.
     * The normal case is to call it from window/surface that has onDraw/display callbacks.
     */
    @Override
    public void renderFrame() {
        if (!hasCalledCreated) {
            throw new IllegalArgumentException(NOT_CALLED_CREATECONTEXT);
        }
        // If renderer is null it means CoreApp is destroyed - do nothing.
        if (renderer != null) {
            try {
                // If multiple threads used this method will return immediately
                componentRunnable.process(rootNode, FrameSampler.getInstance().getDelta());
                renderer.beginFrame();
                clientApp.beginFrame(FrameSampler.getInstance().getDelta());
                if (rootNode != null) {
                    renderer.render(rootNode);
                }
                clientApp.endFrame(FrameSampler.getInstance().getDelta());
                renderer.endFrame();
                if (rootNode != null) {
                    rootNode.swapNodeList();
                }
                if (destroy) {
                    destroy();
                }
                logPerformance();
            } catch (GLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void logPerformance() {
        Sample sample = FrameSampler.getInstance().getSample(Samples.POINTER_INPUT.name());
        if (sample != null) {
            if (sample.nano > 0) {
                int millis = sample.nano / 1000000;
                sample.add(millis);
                FrameSampler.getInstance().autoLog(Samples.POINTER_INPUT.name(), sample);
            }
        }
    }

    /**
     * Sets the scene rootnode, this will update the root node in the logic runnable
     * {@linkplain ComponentProcessorRunnable}
     * A {@linkplain NodeController} will be created for the node and used as {@linkplain EventHandler}
     * 
     * @param node
     */
    public void setRootNode(RootNode node) {
        this.rootNode = node;
        componentRunnable.setRootNode(node);
        ViewController vc = new ViewController();
        vc.registerEventHandler(null);
        NodeController nc = new NodeController(node);
        nc.registerEventHandler(null);
        ComponentHandler.getInstance().initSystems(node, renderer);
        FrameSampler.getInstance().logTag(FrameSampler.Samples.SET_ROOT_NODE);
    }

    /**
     * Adds pointer input callback to the scene, after this call the Node tree will get
     * callbacks on pointer input.
     * Call this if nodes use the {@link EventManager}, eg POINTERINPUT property, or shall use a node with
     * {@link UIElementInput}
     * Set ObjectInputListener on node by calling {@link Node#setObjectInputListener(UIElementInput)}
     * 
     * @param root The rootnode
     */
    public void addPointerInput(RootNode root) {
        CoreInput.getInstance().addMMIListener(new J2SENodeInputListener(root));
    }
    /*
     * public RootNode create(String id) throws NodeException {
     * BaseRootNode root = new BaseRootNode();
     * setRoot(root);
     * // TODO the builder should handle creation of renderpass in a more generic way.
     * Node created = super.create("rootnode");
     * RenderPass pass = new RenderPass();
     * pass.setId("RenderPass");
     * pass.setTarget(new RenderTarget(Target.FRAMEBUFFER, null));
     * pass.setRenderState(new RenderState());
     * pass.setPass(Pass.MAIN);
     * if (created instanceof RenderableNode<?>) {
     * ViewFrustum vf = new ViewFrustum();
     * vf.setOrthoProjection(-0.8889f, 0.8889f, -0.5f, 0.5f, 0, 10);
     * ((RenderableNode<?>) created).setViewFrustum(vf);
     * created.setPass(Pass.ALL);
     * ArrayList<RenderPass> rp = new ArrayList<>();
     * rp.add(pass);
     * ((RenderableNode<?>) created).setRenderPass(rp);
     * }
     * created.onCreated();
     * root.addChild(created);
     * return root;
     * }
     */

    public void displaySplash(int width, int height) throws GLException, NodeException {
        FrameSampler.getInstance().logTag(FrameSampler.Samples.DISPLAY_SPLASH);
        RootNodeBuilder rootBuilder = new RootNodeBuilder();
        RootNode root = rootBuilder.createSplashRoot(renderer, SPLASH_FILENAME, RESOLUTION.ULTRA_HD, width,
                height);
        renderer.beginFrame();
        renderer.render(root);
        renderer.endFrame();
    }

    /**
     * Sets the clientapplication implementation class.
     * This must be called before calling {@link #createCoreApp(int, int, NucleusRenderer)}
     * 
     * @param client Must be a class implementing {@link ClientApplication}
     */
    public static void setClientClass(Type<Object> client) {
        clientClass = client.getTypeClass();
    }

    /**
     * Util method to create the coreapp and display splash. Caller must swapp buffer for splash to be visible.
     * Window and GL context must be created before calling this method.
     * Will set the name of the window from ClientApplication.
     * 
     * @param width Width of display window
     * @param height Height of display window
     * @param renderer The renderer to use
     * @param surfaceConfig The surface configuration
     * @return Instance of {@link CoreApp} with the specified clientclass {@link ClientApplication}
     */
    public static CoreApp createCoreApp(int width, int height, NucleusRenderer renderer,
            SurfaceConfiguration surfaceConfig) {
        if (clientClass == null) {
            throw new IllegalArgumentException("Must call #setClientClass() before calling #createCoreApp()");
        }
        renderer.init(surfaceConfig, width, height);
        try {
            ClientApplication clientApp = (ClientApplication) clientClass.newInstance();
            Window.getInstance().setTitle(clientApp.getAppName() + " " + clientApp.getVersion());
            CoreApp coreApp = new CoreApp(renderer, clientApp);
            try {
                coreApp.displaySplash(width, height);
            } catch (GLException | NodeException e) {
                throw new RuntimeException(e);
            }
            return coreApp;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles back navigation - returns true if the app should be closed.
     * 
     * @return True if app should be closed as a result of back navigation.
     */
    public boolean onBackPressed() {
        int count = NavigationController.getInstance().popBackStackEntry();
        if (count == NavigationController.EMPTY) {
            return true;
        }
        return false;
    }

}
