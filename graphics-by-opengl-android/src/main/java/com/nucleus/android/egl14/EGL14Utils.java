package com.nucleus.android.egl14;

import com.nucleus.renderer.SurfaceConfiguration;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;

/**
 * Utils for Android EGL 1.4 implementation
 * 
 *
 */
public class EGL14Utils {

    /**
     * Reads the configuration and stores in surfaceConfig
     * 
     * @param eglDisplay
     * @param config
     * @param surfaceConfig
     */
    public static void readSurfaceConfig(EGLDisplay eglDisplay, EGLConfig config,
            SurfaceConfiguration surfaceConfig) {
        surfaceConfig.setRedBits(getEGLConfigAttrib(eglDisplay, config, EGL14.EGL_RED_SIZE));
        surfaceConfig.setGreenBits(getEGLConfigAttrib(eglDisplay, config, EGL14.EGL_GREEN_SIZE));
        surfaceConfig.setBlueBits(getEGLConfigAttrib(eglDisplay, config, EGL14.EGL_BLUE_SIZE));
        surfaceConfig.setAlphaBits(getEGLConfigAttrib(eglDisplay, config, EGL14.EGL_ALPHA_SIZE));
        surfaceConfig.setDepthBits(getEGLConfigAttrib(eglDisplay, config, EGL14.EGL_DEPTH_SIZE));
        surfaceConfig.setStencilBits(getEGLConfigAttrib(eglDisplay, config, EGL14.EGL_STENCIL_SIZE));
        surfaceConfig.setSamples(getEGLConfigAttrib(eglDisplay, config, EGL14.EGL_SAMPLES));
        surfaceConfig.setSurfaceType(getEGLConfigAttrib(eglDisplay, config, EGL14.EGL_SURFACE_TYPE));
        setEGLInfo(eglDisplay, surfaceConfig);
    }

    /**
     * Returns a {@link SurfaceConfiguration} with the EGL config.
     * Reads the egl config and stores in a created {@link SurfaceConfiguration}
     * 
     * @param eglDisplay
     * @param config
     * @return The {@link SurfaceConfiguration} for the EGL config
     */
    public static SurfaceConfiguration getSurfaceConfig(EGLDisplay eglDisplay, EGLConfig config) {
        SurfaceConfiguration surfaceConfig = new SurfaceConfiguration();
        readSurfaceConfig(eglDisplay, config, surfaceConfig);
        return surfaceConfig;
    }

    /**
     * Calls getEGLConfigAttrib and returns the value for the specified attribute.
     * 
     * @param eglDisplay
     * @param config
     * @param configAttrib
     * @return
     */
    public static int getEGLConfigAttrib(EGLDisplay eglDisplay, EGLConfig config, int configAttrib) {
        int[] attribs = new int[1];
        EGL14.eglGetConfigAttrib(eglDisplay, config, configAttrib, attribs, 0);
        return attribs[0];
    }

    /**
     * Sets the egl info
     * 
     * @param eglDisplay
     */
    public static void setEGLInfo(EGLDisplay eglDisplay, SurfaceConfiguration surfaceConfig) {
        surfaceConfig.setInfo(EGL14.eglQueryString(eglDisplay, EGL14.EGL_VERSION),
                EGL14.eglQueryString(eglDisplay, EGL14.EGL_VENDOR),
                EGL14.eglQueryString(eglDisplay, EGL14.EGL_EXTENSIONS));
    }

}
