package com.nucleus.vulkan.lwjgl3;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryUtil;

import com.nucleus.Backend;
import com.nucleus.Backend.BackendFactory;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.J2SEWindowApplication.PropertySettings;
import com.nucleus.lwjgl3.GLFWWindow;
import com.nucleus.renderer.NucleusRenderer.Renderers;

/**
 * Window for Vulkan support
 *
 */
public class GLFWVulkanWindow extends GLFWWindow {

    public GLFWVulkanWindow(BackendFactory factory, CoreAppStarter coreAppStarter, PropertySettings appSettings) {
        super(factory, coreAppStarter, appSettings);
    }

    @Override
    public VideoMode init(PropertySettings appSettings) {
        GLFWErrorCallback.createPrint().set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize glfw");
        }
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        window = GLFW.glfwCreateWindow(appSettings.width, appSettings.height, "", MemoryUtil.NULL,
                MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        backend = initFW(window, appSettings);
        initInput();
        return new VideoMode(appSettings.width, appSettings.height, appSettings.fullscreen, appSettings.swapInterval);
    }

    @Override
    protected Backend initFW(long GLFWWindow, PropertySettings appSettings) {
        return factory.createBackend(Renderers.VULKAN11, window, null);
    }

}
