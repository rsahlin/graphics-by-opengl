package com.nucleus.opengl.lwjgl3;

import java.util.Objects;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.system.Configuration;

import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.lwjgl3.GLFWWindow;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

/**
 * Window for GLFW GLES support
 *
 */
public class GLFWGLESWindow extends GLFWWindow {

    private GLESCapabilities gles;

    public GLFWGLESWindow(Renderers version, CoreAppStarter coreAppStarter, SurfaceConfiguration config, int width,
            int height) {
        super(version, coreAppStarter, config, width, height);
    }

    @Override
    public void drawFrame() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GLES.setCapabilities(gles);
        super.drawFrame();
    }

    @Override
    protected void initFW(long GLFWWindow) {
        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = Objects.requireNonNull(GLFW.glfwGetVideoMode(monitor));

        GLFW.glfwMakeContextCurrent(window);
        Configuration.OPENGLES_EXPLICIT_INIT.set(true);
        GLES.create(GL.getFunctionProvider());
        gles = GLES.createCapabilities();
        backend = LWJGLWrapperFactory.createGLESWrapper(LWJGLWrapperFactory.getGLESVersion(gles));
    }

}
