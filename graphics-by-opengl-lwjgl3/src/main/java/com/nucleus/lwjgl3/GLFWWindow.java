package com.nucleus.lwjgl3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.system.MemoryUtil;

import com.nucleus.Backend;
import com.nucleus.Backend.BackendFactory;
import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.J2SEWindowApplication.PropertySettings;
import com.nucleus.SimpleLogger;
import com.nucleus.common.Environment;
import com.nucleus.common.Environment.Property;
import com.nucleus.mmi.Key.Action;
import com.nucleus.mmi.Pointer.PointerAction;
import com.nucleus.mmi.Pointer.Type;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

/**
 * The main window implementation for GLFW on LWJGL, windows will be created with GLFW
 * 
 *
 */
public abstract class GLFWWindow extends J2SEWindow {

    public static final int DEFAULT_MONITOR_INDEX = 0;

    protected static final int MAX_MOUSE_BUTTONS = 3;
    protected long window;
    protected PointerBuffer monitors;
    protected int[] buttonActions = new int[MAX_MOUSE_BUTTONS];
    protected int[] cursorPosition = new int[2];
    protected Hashtable<Integer, Integer> GLFWKeycodes;
    protected int monitorIndex = DEFAULT_MONITOR_INDEX;

    /**
     * 
     * @param coreAppStarter
     * @param width
     * @param height
     */
    public GLFWWindow(BackendFactory factory, CoreApp.CoreAppStarter coreAppStarter, PropertySettings appSettings) {
        super(factory, coreAppStarter, appSettings);
    }

    @Override
    public VideoMode init(PropertySettings appSettings) {
        GLFWErrorCallback.createPrint().set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize glfw");
        }
        SimpleLogger.d(getClass(), "GLFW version :" + GLFW.glfwGetVersionString());
        monitors = getMonitors();
        long monitor = monitors.get(monitorIndex);
        Size size = createGLFWWindow(appSettings, monitor);
        SimpleLogger.d(getClass(),
                "Initializing GLFW window for requested version " + appSettings.version);
        if (appSettings.nativeGLES) {
            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_ES_API);
        }
        if (appSettings.forceVersion == true) {
            Renderers version = appSettings.setDriverVersion != null ? appSettings.setDriverVersion
                    : appSettings.version;
            SimpleLogger.d(getClass(), "Forcing GLFW GLES version to " + version);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, version.major);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, version.minor);
        }
        backend = initFW(window, appSettings);
        GLFW.glfwSwapInterval(appSettings.swapInterval);
        initInput();
        return new VideoMode(size, appSettings.fullscreen, appSettings.swapInterval);
    }

    private void setWindowHints(SurfaceConfiguration config) {
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, config.getSamples());
        GLFW.glfwWindowHint(GLFW.GLFW_SCALE_TO_MONITOR, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);
        SimpleLogger.d(getClass(), "Set samples: " + config.getSamples());
    }

    protected Size createGLFWWindow(PropertySettings appSettings, long monitor) {
        SurfaceConfiguration config = appSettings.getConfiguration();
        setWindowHints(config);
        listVideoModes(monitor);
        if (appSettings.fullscreen) {
            VideoMode requested = new VideoMode(appSettings.width, appSettings.height, appSettings.fullscreen,
                    appSettings.swapInterval);
            createFullscreen(monitor, requested);
        } else {
            SimpleLogger.d(getClass(),
                    "Creating window with size " + appSettings.width + ", " + appSettings.height);
            window = GLFW.glfwCreateWindow(appSettings.width, appSettings.height, "", MemoryUtil.NULL, MemoryUtil.NULL);
        }
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        Size size = getFrameBufferSize(window);
        SimpleLogger.d(getClass(), size.toString());
        return size;
    }

    protected void createFullscreen(long monitor, VideoMode requested) {
        SimpleLogger.d(getClass(), "Fullscreen resolution " + requested.getWidth() + ", " + requested.getHeight());
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
        float[] x = new float[1];
        float[] y = new float[1];
        GLFW.glfwGetWindowContentScale(monitor, x, y);
        SimpleLogger.d(getClass(), "Current monitor resolution " + vidMode.width() + ", " + vidMode.height()
                + " and scale: " + x[0] + ", " + y[0]);
        if (requested.getWidth() == vidMode.width() && requested.getHeight() == vidMode.height()) {
            SimpleLogger.d(getClass(), "Setting current monitor to fullscreen");
            setVideoMode(requested, monitorIndex);
        } else {
            GLFW.glfwWindowHint(GLFW.GLFW_ICONIFIED, GLFW.GLFW_FALSE);
            SimpleLogger.d(getClass(), "Creating new window");
            window = createWindow(requested.getWidth(), requested.getHeight(), monitor);
            GLFWVidMode m = GLFW.glfwGetVideoMode(monitor);
            if (m.width() != requested.getWidth() || m.height() != requested.getHeight()) {
                throw new IllegalArgumentException("Could not create videomode");
            }
        }
    }

    private Size getFrameBufferSize(long window) {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetFramebufferSize(window, width, height);
        return new Size(width[0], height[0]);
    }

    private long createWindow(int width, int height, long monitor) {
        SimpleLogger.d(getClass(),
                "Creating fullscreen window with size " + width + ", " + height);
        long window = GLFW.glfwCreateWindow(width, height, "", monitor,
                MemoryUtil.NULL);
        return window;
    }

    private PointerBuffer getMonitors() {
        PointerBuffer monitors = GLFW.glfwGetMonitors();
        if (monitors.capacity() > 0) {
            SimpleLogger.d(getClass(), "Found " + monitors.capacity() + " monitors.");
        } else {
            SimpleLogger.d(getClass(), "glfwGetMonitors() returns zero monitors.");

        }
        return monitors;
    }

    private List<Size> listVideoModes(long monitor) {
        GLFWVidMode.Buffer buffer = GLFW.glfwGetVideoModes(monitor);
        ArrayList<Size> sizes = new ArrayList<Size>();
        while (buffer.hasRemaining()) {
            GLFWVidMode mode = buffer.get();
            SimpleLogger.d(getClass(), "Found videomode " + mode.width() + ", " + mode.height() + ", " +
                    mode.redBits() + "." + mode.greenBits() + "." + mode.blueBits() + ", refresh: "
                    + mode.refreshRate());
            Size s = new Size(mode.width(), mode.height());
            if (!sizes.contains(s)) {
                sizes.add(s);
            }
        }
        if (Environment.getInstance().isProperty(Property.TEST_FULLSCREEN_MODES, false)) {
            // Test if fullscreen modes are created properly.
            SimpleLogger.d(getClass(), "Testing " + sizes.size() + " monitor modes.");
            int[] width = new int[1];
            int[] height = new int[1];
            for (Size s : sizes) {
                SimpleLogger.d(getClass(), "Creating window with size " + s.toString() + " for monitor " + monitor);
                long win = GLFW.glfwCreateWindow(s.getWidth(), s.getHeight(), "", monitor, MemoryUtil.NULL);
                GLFW.glfwGetFramebufferSize(win, width, height);
                Size framebuffer = new Size(width[0], height[0]);
                GLFW.glfwGetWindowSize(win, width, height);
                Size windowSize = new Size(width[0], height[0]);
                if (framebuffer.equals(s)) {
                    SimpleLogger.d(getClass(), "SUCCESS for monitor mode " + s.toString());
                } else {
                    SimpleLogger.d(getClass(),
                            "FAILED for monitor mode " + s.toString() + ", real size " + framebuffer);
                }
                GLFW.glfwDestroyWindow(win);
            }
        }
        return sizes;
    }

    /**
     * Initialises the glfw render framework, GLES/Vulkan etc
     * GLFW is already initialized before calling this method and the window handle is created
     * 
     * @param GLFWWindow
     */
    protected abstract Backend initFW(long GLFWWindow, PropertySettings appSettings);

    protected void initInput() {
        /**
         * Fetch scancode for fields that start with VK_ and store keycodes in array to convert scancode to AWT values
         */
        GLFWKeycodes = getGLFWKeys();

        GLFW.glfwSetKeyCallback(window, (windowHnd, key, scancode, action, mods) -> {
            Integer awtKey = GLFWKeycodes.get(key);
            if (awtKey != null) {
                switch (action) {
                    case GLFW.GLFW_RELEASE:
                        super.handleKeyEvent(new com.nucleus.mmi.Key(Action.RELEASED, key));
                        if (key == GLFW.GLFW_KEY_ESCAPE) {
                            onBackPressed();
                        }
                        break;
                    case GLFW.GLFW_PRESS:
                        super.handleKeyEvent(new com.nucleus.mmi.Key(Action.PRESSED, key));
                        break;
                }
            } else {
                SimpleLogger.d(getClass(), "No AWT keycode for: " + key);
            }
        });

        GLFW.glfwSetCursorPosCallback(window, new GLFWCursorPosCallbackI() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                cursorPosition[0] = (int) xpos;
                cursorPosition[1] = (int) ypos;
                if (buttonActions[0] == GLFW.GLFW_PRESS) {
                    handleMouseEvent(PointerAction.MOVE, Type.MOUSE, cursorPosition[0], cursorPosition[1], 0,
                            System.currentTimeMillis());
                }
            }
        });
        GLFW.glfwSetWindowCloseCallback(window, new GLFWWindowCloseCallbackI() {
            @Override
            public void invoke(long window) {
                SimpleLogger.d(getClass(), "Window closed");
                windowClosed();
                destroy();
                System.exit(0);
            }
        });

        GLFW.glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallbackI() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button >= 0 && button < buttonActions.length) {
                    buttonActions[button] = action;
                    switch (action) {
                        case GLFW.GLFW_PRESS:
                            handleMouseEvent(PointerAction.DOWN, Type.MOUSE, cursorPosition[0], cursorPosition[1], 0,
                                    System.currentTimeMillis());
                            break;
                        case GLFW.GLFW_RELEASE:
                            handleMouseEvent(PointerAction.UP, Type.MOUSE, cursorPosition[0], cursorPosition[1], 0,
                                    System.currentTimeMillis());
                            break;
                    }
                }
            }
        });

        GLFW.glfwSetScrollCallback(window, new GLFWScrollCallbackI() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                mouseWheelMoved((int) yoffset, System.currentTimeMillis());
            }
        });

    }

    protected Hashtable<Integer, Integer> getGLFWKeys() {
        Hashtable<Integer, Integer> GLFWFields = new Hashtable<>();
        for (Field scanField : GLFW.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(scanField.getModifiers())) {
                String fieldName = scanField.getName();
                if (fieldName.startsWith("GLFW_KEY_")) {
                    String key = fieldName.substring(9);

                    try {
                        Field awtField = java.awt.event.KeyEvent.class.getField("VK_" + key);
                        Field field = GLFW.class.getField(fieldName);
                        int scanCode = field.getInt(null);
                        int awtKeyCode = awtField.getInt(null);
                        GLFWFields.put(scanCode, awtKeyCode);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        SimpleLogger.d(getClass(), e.toString());
                    }
                }
            }
        }
        return GLFWFields;
    }

    @Override
    public void drawFrame() {
        coreApp.renderFrame();
        GLFW.glfwSwapBuffers(window); // swap the color buffers
        // Poll for window events. The key callback above will only be
        // invoked during this call
        GLFW.glfwPollEvents();
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(window); // swap the color buffers
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
    public VideoMode setVideoMode(VideoMode videoMode, int monitorIndex) {
        long monitor = monitors.get(monitorIndex);
        VideoMode result = videoMode;
        if (monitorIndex < monitors.capacity()) {
            if (videoMode.isFullScreen()) {
                GLFW.glfwSetWindowMonitor(window, monitor, 0, 0, videoMode.getWidth(), videoMode.getHeight(),
                        GLFW.GLFW_DONT_CARE);
                GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
                SimpleLogger.d(getClass(), "Set monitor resolution to " + vidMode.width() + ", " + vidMode.height());
                // GLFW.glfwSetWindowIconifyCallback(monitor, this);
                result = new VideoMode(vidMode.width(), vidMode.height(), true, videoMode.getSwapInterval());
            } else {
                GLFW.glfwSetWindowMonitor(window, MemoryUtil.NULL, 100, 100, videoMode.getWidth(),
                        videoMode.getHeight(),
                        GLFW.GLFW_DONT_CARE);
            }
        } else {
            SimpleLogger.d(getClass(), "Invalid monitor index " + monitorIndex);
        }
        return result;
    }

    @Override
    public void destroy() {
        SimpleLogger.d(getClass(), "destroy()");
        GLFW.glfwDestroyWindow(window);
        window = 0;
    }

}
