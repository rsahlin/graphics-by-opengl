package com.nucleus.opengl.lwjgl3;

import java.nio.ByteBuffer;

import org.lwjgl.egl.EGL;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;
import org.lwjgl.system.SharedLibrary;
import org.lwjgl.system.ThreadLocalUtil;

import com.nucleus.Backend;
import com.nucleus.Backend.BackendFactory;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.SimpleLogger;
import com.nucleus.lwjgl3.GLFWWindow;

/**
 * Window for GLFW GLES support
 *
 */
public class GLFWGLESWindow extends GLFWWindow {

    private GLESCapabilities gles;

    public GLFWGLESWindow(BackendFactory factory, CoreAppStarter coreAppStarter, Configuration config) {
        super(factory, coreAppStarter, config);
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
    protected Backend initFW(long GLFWWindow) {
        GLFW.glfwMakeContextCurrent(window);
        org.lwjgl.system.Configuration.OPENGLES_EXPLICIT_INIT.set(true);
        if (configuration.nativeGLES) {
            GLES.create(GLES.getFunctionProvider());
            gles = GLES.createCapabilities();
        } else {
            GLES.create(GL.getFunctionProvider());
            gles = GLES.createCapabilities();
        }
        SimpleLogger.d(getClass(), "GLCapabilities with support for: \nGLES20: " + gles.GLES20
                + "\nGLES30: " + gles.GLES30
                + "\nGLES31: " + gles.GLES31
                + "\nGLES32: " + gles.GLES32);
        return factory.createBackend(configuration.version, window, null);
    }

    /** Loads the OpenGL ES native library, using the default library name. */
    public void create() {
        SharedLibrary gles;
        switch (Platform.get()) {
            case LINUX:
                gles = Library.loadNative(GLES.class, "com.super2k.opengl",
                        org.lwjgl.system.Configuration.OPENGLES_LIBRARY_NAME,
                        "libGLESv2.so.2");
                break;
            case MACOSX:
                gles = Library.loadNative(GLES.class, "com.super2k.opengl",
                        org.lwjgl.system.Configuration.OPENGLES_LIBRARY_NAME,
                        "GLESv2");
                break;
            case WINDOWS:
                gles = Library.loadNative(GLES.class, "com.super2k.opengl",
                        org.lwjgl.system.Configuration.OPENGLES_LIBRARY_NAME,
                        "libGLESv2", "GLESv2");
                break;
            default:
                throw new IllegalStateException();
        }
        create(gles);
    }

    private void create(SharedLibrary gles) {
        try {
            FunctionProvider egl = EGL.getFunctionProvider();
            if (egl == null) {
                throw new IllegalStateException("The EGL function provider is not available.");
            }

            create((FunctionProvider) new SharedLibrary.Delegate(gles) {
                @Override
                public long getFunctionAddress(ByteBuffer functionName) {
                    long address = egl.getFunctionAddress(functionName);
                    if (address == MemoryUtil.NULL) {
                        address = library.getFunctionAddress(functionName);
                        if (address == MemoryUtil.NULL) {
                            SimpleLogger.d(getClass(), "Failed to locate address for GLES function " + functionName);
                        }
                    }

                    return address;
                }
            });
        } catch (RuntimeException e) {
            gles.free();
            throw e;
        }
    }

    private FunctionProvider createFunctionProvider(SharedLibrary gles) {
        FunctionProvider egl = EGL.getFunctionProvider();
        return new SharedLibrary.Delegate(gles) {
            @Override
            public long getFunctionAddress(ByteBuffer functionName) {
                long address = egl.getFunctionAddress(functionName);
                if (address == MemoryUtil.NULL) {
                    address = library.getFunctionAddress(functionName);
                    if (address == MemoryUtil.NULL) {
                        SimpleLogger.d(getClass(), "Failed to locate address for GLES function " + functionName);
                    }
                }

                return address;
            }
        };
    }

    /**
     * Initializes OpenGL ES with the specified {@link FunctionProvider}. This method can be used to implement custom
     * OpenGL ES library loading.
     *
     * @param functionProvider the provider of OpenGL ES function addresses
     */
    public void create(FunctionProvider functionProvider) {
        if (GLES.getFunctionProvider() != null) {
            throw new IllegalStateException("OpenGL ES has already been created.");
        }

        GLES.create(functionProvider);
        ThreadLocalUtil.setFunctionMissingAddresses(GLESCapabilities.class, 3);
    }

}
