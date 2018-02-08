package com.nucleus.lwjgl3;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.opengl.GLESWrapper.Renderers;

/**
 * The lwjgl3 window
 *
 */
public class GLFWWindow extends J2SEWindow {

    // The window handle
    private long window;
    private GLESCapabilities gles;

    /**
     * 
     * @param coreAppStarter
     * @param width
     * @param height
     */
    public GLFWWindow(Renderers version, CoreApp.CoreAppStarter coreAppStarter, int width, int height) {
        super(coreAppStarter, width, height);
        init(version, coreAppStarter, width, height);
    }

    private void init(Renderers version, CoreApp.CoreAppStarter coreAppStarter, int width, int height) {
        GLFWErrorCallback.createPrint().set();
        // pretend we're using GLES in windows, instead use a subset of OpenGL 2.0 as GLES 2.0
        // Bypasses the default create() method.
        Configuration.OPENGLES_EXPLICIT_INIT.set(true);
        GLES.create(GL.getFunctionProvider());
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize glfw");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        // GLFW setup for EGL & OpenGL ES
        // GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_EGL_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);

        window = GLFW.glfwCreateWindow(width, height, "GLFW EGL/OpenGL ES Demo", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwSetKeyCallback(window, (windowHnd, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_ESCAPE) {
                GLFW.glfwSetWindowShouldClose(windowHnd, true);
            }
        });
        /*
         * // EGL capabilities
         * long dpy = GLFWNativeEGL.glfwGetEGLDisplay();
         * 
         * EGLCapabilities egl;
         * try (MemoryStack stack = MemoryStack.stackPush()) {
         * IntBuffer major = stack.mallocInt(1);
         * IntBuffer minor = stack.mallocInt(1);
         * 
         * if (!EGL10.eglInitialize(dpy, major, minor)) {
         * throw new IllegalStateException(String.format("Failed to initialize EGL [0x%X]", EGL10.eglGetError()));
         * }
         * 
         * egl = EGL.createDisplayCapabilities(dpy, major.get(0), minor.get(0));
         * }
         * 
         * try {
         * SimpleLogger.d(getClass(), "EGL Capabilities:");
         * for (Field f : EGLCapabilities.class.getFields()) {
         * if (f.getType() == boolean.class) {
         * if (f.get(egl).equals(Boolean.TRUE)) {
         * SimpleLogger.d(getClass(), "\t" + f.getName());
         * }
         * }
         * }
         * } catch (IllegalAccessException e) {
         * e.printStackTrace();
         * }
         */
        // OpenGL ES capabilities
        GLFW.glfwMakeContextCurrent(window);
        gles = GLES.createCapabilities();
        // Render with OpenGL ES
        GLFW.glfwShowWindow(window);
        wrapper = new LWJGL3GLES30Wrapper();
    }

    @Override
    public void drawFrame() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GLES.setCapabilities(gles);
        coreApp.drawFrame();
        GLFW.glfwSwapBuffers(window); // swap the color buffers
        // Poll for window events. The key callback above will only be
        // invoked during this call.
        GLFW.glfwPollEvents();
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(window); // swap the color buffers
        GLFW.glfwSwapInterval(1);
    }

}
