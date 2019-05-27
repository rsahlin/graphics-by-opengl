package com.nucleus.opengl.lwjgl3;

import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.Objects;

import org.lwjgl.egl.EGL;
import org.lwjgl.egl.EGL10;
import org.lwjgl.egl.EGLCapabilities;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWNativeEGL;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.SimpleLogger;
import com.nucleus.common.Environment;
import com.nucleus.egl.EGLUtils;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

public class LWJGLEGLWindow extends J2SEWindow implements Runnable {

    Thread thread;

    protected Renderers version;
    protected SurfaceConfiguration surfaceConfig;
    protected RenderContextListener renderListener;
    protected long window;
    protected GLESCapabilities gles;
    Environment env;
    /**
     * Special surface attribs that may be specified when creating the surface - see
     * https://www.khronos.org/registry/EGL/sdk/docs/man/html/eglCreateWindowSurface.xhtml
     * Shall be terminateded by EGL_NONE
     * EGL_RENDER_BUFFER
     * EGL_VG_ALPHA_FORMAT
     * EGL_VG_COLORSPACE
     */
    protected int[] surfaceAttribs;

    public LWJGLEGLWindow(Renderers version, CoreApp.CoreAppStarter coreAppStarter, SurfaceConfiguration config,
            int width, int height) {
        super(coreAppStarter, width, height, config);
        env = Environment.getInstance();
        this.version = version;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Creates a list of egl config attribs for the surface config, if surfaceConfig is null then
     * {@link #createDefaultConfigAttribs()} is called.
     * 
     * @param surfaceConfig
     * @return
     */
    protected int[] createEGLConfigAttribs(SurfaceConfiguration surfaceConfig) {
        if (surfaceConfig != null) {
            return EGLUtils.createConfig(surfaceConfig);
        } else {
            // Create default.
            return createDefaultConfigAttribs();
        }
    }

    /**
     * Creates the egl context, default is to set client version to {@link #version#major}.
     * 
     * @throws IllegalArgumentException If context could not be created
     */
    protected void createEGLContext() {
    }

    /**
     * Choose the desired config
     */
    protected void chooseEGLConfig() {
    }

    /**
     * Creates the egl context, first getting the display by calling
     */
    protected void createEglContext() {
        GLFWErrorCallback.createPrint().set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize glfw");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLES20.GL_TRUE);

        // GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_EGL_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        // GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_ES_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);

        // pretend we're using GLES in windows, instead use a subset of OpenGL 2.0 as GLES 2.0
        // Bypasses the default create() method.
        // Configuration.EGL_LIBRARY_NAME.set(Pointer.BITS64 ? "libEGL32" : "libEGL32");
        // Configuration.EGL_EXPLICIT_INIT.set(true);
        // EGL.create(EGL.getFunctionProvider());
        // GLES.create(GL.getFunctionProvider());
        // Configuration.OPENGLES_LIBRARY_NAME.set("opengl32");
        // Configuration.OPENGLES_EXPLICIT_INIT.set(true);

        // GLES.create(GLES.getFunctionProvider()); // omg?!

        int WIDTH = 300;
        int HEIGHT = 300;

        window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        long monitor = GLFW.glfwGetPrimaryMonitor();

        GLFWVidMode vidmode = Objects.requireNonNull(GLFW.glfwGetVideoMode(monitor));
        GLFW.glfwMakeContextCurrent(window);

        Configuration.OPENGLES_EXPLICIT_INIT.set(true);
        GLES.create(GL.getFunctionProvider());
        gles = GLES.createCapabilities();

        GLFW.glfwSetKeyCallback(window, (windowHnd, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_ESCAPE) {
                GLFW.glfwSetWindowShouldClose(windowHnd, true);
            }
        });

        GLFW.glfwSetKeyCallback(window, (windowHnd, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_ESCAPE) {
                GLFW.glfwSetWindowShouldClose(windowHnd, true);
            }
        });

        // EGL capabilities
        long dpy = GLFWNativeEGL.glfwGetEGLDisplay();
        if (dpy == EGL10.EGL_NO_DISPLAY) {
            throw new IllegalArgumentException("EGL_NO_DISPLAY");
        }

        EGLCapabilities egl;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer major = stack.mallocInt(1);
            IntBuffer minor = stack.mallocInt(1);

            if (!EGL10.eglInitialize(dpy, major, minor)) {
                throw new IllegalStateException(String.format("Failed to initialize EGL [0x%X]", EGL10.eglGetError()));
            }

            egl = EGL.createDisplayCapabilities(dpy, major.get(0), minor.get(0));
        }

        try {
            System.out.println("EGL Capabilities:");
            for (Field f : EGLCapabilities.class.getFields()) {
                if (f.getType() == boolean.class) {
                    if (f.get(egl).equals(Boolean.TRUE)) {
                        System.out.println("\t" + f.getName());
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("OpenGL ES Capabilities:");
            for (Field f : GLESCapabilities.class.getFields()) {
                if (f.getType() == boolean.class) {
                    if (f.get(gles).equals(Boolean.TRUE)) {
                        System.out.println("\t" + f.getName());
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    protected int[] createDefaultConfigAttribs() {
        return null;
    }

    protected void createEglSurface() {
    }

    protected void createEGL() {
        createEglContext();
        createEglSurface();
        makeCurrent();
    }

    protected void makeCurrent() {
    }

    public void setRenderContextListener(RenderContextListener listener) {
        this.renderListener = listener;
    }

    /**
     * Sets the egl swap interval, if no EGLDisplay exists then nothing is done.
     * 
     * @param interval
     */
    public void setEGLSwapInterval(int interval) {
    }

    /**
     * Sets an egl surfaceattrib, if EGLDisplay or EGLSurface is null then nothing is done.
     * 
     * @param attribute
     * @param value
     */
    public void setEGLSurfaceAttrib(int attribute, int value) {
    }

    @Override
    public void run() {
        SimpleLogger.d(getClass(), "Starting EGL surface thread");
        createEGL();
        // while (surface != null) {
        internalDoFrame();
        // }
        if (renderListener != null) {
            renderListener.surfaceLost();
        }
        SimpleLogger.d(getClass(), "Exiting surface thread");
        thread = null;
    }

    protected void internalDoFrame() {
        drawFrame();
        // if (EGLSurface != null) {
        swapBuffers();
        // }
    }

    /**
     * Swapbuffers and syncronize
     */
    protected void swapBuffers() {
        Environment env = Environment.getInstance();
        long start = System.currentTimeMillis();
        // EGL14.eglSwapBuffers(EglDisplay, EGLSurface);
        boolean eglWaitGL = env.isProperty(Environment.Property.EGLWAITGL, false);
        if (eglWaitGL) {
            // EGL14.eglWaitGL();
        }
        FrameSampler.getInstance().addTag(FrameSampler.Samples.EGLSWAPBUFFERS.name() + "-WAITGL=" + eglWaitGL,
                start,
                System.currentTimeMillis(), FrameSampler.Samples.EGLSWAPBUFFERS.detail);
    }

    @Override
    public void internalCreateCoreApp(int width, int height) {
        backend = LWJGLWrapperFactory.createGLESWrapper(LWJGLWrapperFactory.getGLESVersion(gles));
        super.internalCreateCoreApp(width, height);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            GLFW.glfwShowWindow(window);
        } else {
            GLFW.glfwHideWindow(window);
        }
    }

    @Override
    public void setWindowTitle(String title) {
        if (window != 0) {
            GLFW.glfwSetWindowTitle(window, title);
        }
    }

    @Override
    protected void setFullscreenMode(boolean fullscreen) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    protected void destroy() {
        throw new IllegalArgumentException("Not implemented");
    }

}
