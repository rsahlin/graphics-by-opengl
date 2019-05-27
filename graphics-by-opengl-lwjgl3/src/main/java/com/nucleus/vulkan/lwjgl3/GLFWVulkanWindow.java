package com.nucleus.vulkan.lwjgl3;

import java.util.Objects;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVulkan;

import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.lwjgl3.GLFWWindow;
import com.nucleus.opengl.lwjgl3.LWJGLWrapperFactory;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

/**
 * Window for Vulkan support
 *
 */
public class GLFWVulkanWindow extends GLFWWindow {

    public GLFWVulkanWindow(Renderers version, CoreAppStarter coreAppStarter, SurfaceConfiguration config, int width,
            int height) {
        super(version, coreAppStarter, config, width, height);
    }

    @Override
    protected void initFW(long GLFWWindow) {
        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver (ICD)");
        }
        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = Objects.requireNonNull(GLFW.glfwGetVideoMode(monitor));
        GLFW.glfwMakeContextCurrent(window);
        // Configuration.OPENGLES_EXPLICIT_INIT.set(true);
        // GLES.create(GL.getFunctionProvider());
        // gles = GLES.createCapabilities();
        backend = LWJGLWrapperFactory.createVulkanWrapper(Renderers.VULKAN11);
    }

}
