package com.nucleus.lwjgl3;

import java.util.Objects;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

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

        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        // GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLES20.GL_TRUE);

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);

        window = GLFW.glfwCreateWindow(width, height, "GLFW EGL/OpenGL ES Demo", MemoryUtil.NULL, MemoryUtil.NULL);
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
    }

    @Override
    public void internalCreateCoreApp(int width, int height) {
        wrapper = LWJGLWrapperFactory.createWrapper(gles, null);
        super.internalCreateCoreApp(width, height);
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
        // invoked during this call.
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

}
