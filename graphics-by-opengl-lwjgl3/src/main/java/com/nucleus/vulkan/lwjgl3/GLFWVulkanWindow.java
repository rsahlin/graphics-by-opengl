package com.nucleus.vulkan.lwjgl3;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryUtil;

import com.nucleus.Backend;
import com.nucleus.Backend.BackendFactory;
import com.nucleus.CoreApp;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.lwjgl3.GLFWWindow;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

/**
 * Window for Vulkan support
 *
 */
public class GLFWVulkanWindow extends GLFWWindow {

    public GLFWVulkanWindow(Renderers version, BackendFactory factory, CoreAppStarter coreAppStarter,
            SurfaceConfiguration config, int width,
            int height) {
        super(version, factory, coreAppStarter, config, width, height);
    }

    @Override
    protected void init(Renderers version, BackendFactory factory, CoreApp.CoreAppStarter coreAppStarter, int width,
            int height) {
        this.factory = factory;
        GLFWErrorCallback.createPrint().set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize glfw");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
        window = GLFW.glfwCreateWindow(width, height, "", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        backend = initFW(window);
        initInput();
    }

    @Override
    protected Backend initFW(long GLFWWindow) {
        return factory.createBackend(Renderers.VULKAN11, window, null);
    }

}
