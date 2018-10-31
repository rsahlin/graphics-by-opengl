package com.nucleus.lwjgl3;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Objects;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.SimpleLogger;
import com.nucleus.mmi.KeyEvent.Action;
import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.mmi.PointerData.Type;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

/**
 * The main window implementation for GLFW on LWJGL, windows will be created with GLFW
 * 
 *
 */
public class GLFWWindow extends J2SEWindow {

    private static final int MAX_MOUSE_BUTTONS = 3;
    // The window handle
    private long window;
    private GLESCapabilities gles;
    private int[] buttonActions = new int[MAX_MOUSE_BUTTONS];
    private int[] cursorPosition = new int[2];
    private Hashtable<Integer, Integer> GLFWKeycodes;

    /**
     * 
     * @param coreAppStarter
     * @param width
     * @param height
     */
    public GLFWWindow(Renderers version, CoreApp.CoreAppStarter coreAppStarter, SurfaceConfiguration config, int width,
            int height) {
        super(coreAppStarter, width, height, config);
        init(version, coreAppStarter, width, height);
    }

    private void init(Renderers version, CoreApp.CoreAppStarter coreAppStarter, int width, int height) {
        GLFWErrorCallback.createPrint().set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize glfw");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        // GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        // GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLES20.GL_TRUE);

        // GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        // GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        // GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_ES_API);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, config.getSamples());
        window = GLFW.glfwCreateWindow(width, height, "", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        long monitor = GLFW.glfwGetPrimaryMonitor();

        GLFWVidMode vidmode = Objects.requireNonNull(GLFW.glfwGetVideoMode(monitor));
        GLFW.glfwMakeContextCurrent(window);

        Configuration.OPENGLES_EXPLICIT_INIT.set(true);
        GLES.create(GL.getFunctionProvider());
        gles = GLES.createCapabilities();
        wrapper = LWJGLWrapperFactory.createWrapper(gles, null);

        /**
         * Fetch scancode for fields that start with VK_ and store keycodes in array to convert scancode to AWT values
         */
        GLFWKeycodes = getGLFWKeys();

        GLFW.glfwSetKeyCallback(window, (windowHnd, key, scancode, action, mods) -> {
            switch (action) {
                case GLFW.GLFW_RELEASE:
                    super.handleKeyEvent(new com.nucleus.mmi.KeyEvent(Action.RELEASED, key));
                    if (key == GLFW.GLFW_KEY_ESCAPE) {
                        backPressed();
                    }
                    break;
                case GLFW.GLFW_PRESS:
                    super.handleKeyEvent(new com.nucleus.mmi.KeyEvent(Action.PRESSED, key));
                    break;
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

    private Hashtable<Integer, Integer> getGLFWKeys() {
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
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GLES.setCapabilities(gles);
        coreApp.renderFrame();
        GLFW.glfwSwapBuffers(window); // swap the color buffers
        // Poll for window events. The key callback above will only be
        // invoked during this call
        GLFW.glfwPollEvents();
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(window); // swap the color buffers
        GLFW.glfwSwapInterval(1);
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
        SimpleLogger.d(getClass(), "destroy()");
        GLFW.glfwDestroyWindow(window);
        window = 0;
    }

}
