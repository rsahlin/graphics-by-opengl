package com.nucleus.egl;

import java.util.ArrayList;

import com.nucleus.renderer.SurfaceConfiguration;

public class EGLUtils {

    private static final int[] EGL_SURFACE_TYPES = new int[] { EGL14Constants.EGL_PBUFFER_BIT,
            EGL14Constants.EGL_PIXMAP_BIT,
            EGL14Constants.EGL_SWAP_BEHAVIOR_PRESERVED_BIT, EGL14Constants.EGL_VG_ALPHA_FORMAT_PRE_BIT,
            EGL14Constants.EGL_VG_COLORSPACE_LINEAR_BIT, EGL14Constants.EGL_WINDOW_BIT };
    /**
     * MUST MATCH CONTENTS in {@link #EGL_SURFACE_TYPES}
     */
    private static final String[] EGL_SURFACE_TYPES_STRING = new String[] { "EGL_PBUFFER_BIT", "EGL_PIXMAP_BIT",
            "EGL_SWAP_BEHAVIOR_PRESERVED_BIT", "EGL_VG_ALPHA_FORMAT_PRE_BIT",
            "EGL_VG_COLORSPACE_LINEAR_BIT", "EGL_WINDOW_BIT" };

    /**
     * Returns the surface type bitmask as String
     * 
     * @param surfaceType EGL Surface type bitmask
     * @return String description of surface type bitmask or empty string is value not set.
     */
    public static String getSurfaceTypeAsString(int surfaceType) {
        StringBuilder sb = new StringBuilder();
        String prepend = "";
        int count = EGL_SURFACE_TYPES.length;
        for (int index = 0; index < count; index++) {
            switch (surfaceType & EGL_SURFACE_TYPES[index]) {
                case 0:
                    break;
                default:
                    sb.append(prepend + EGL_SURFACE_TYPES_STRING[index]);
                    prepend = " | ";
            }
        }
        return sb.toString();
    }

    /**
     * Creates int array for the specified surface configuration
     * 
     * @param egl
     * @param display
     * @param wantedConfig
     * @return
     */
    public static int[] createConfig(SurfaceConfiguration wantedConfig) {
        ArrayList<int[]> eglArray = new ArrayList<int[]>();
        setConfig(eglArray, EGL14Constants.EGL_RED_SIZE, wantedConfig.getRedBits());
        setConfig(eglArray, EGL14Constants.EGL_GREEN_SIZE, wantedConfig.getGreenBits());
        setConfig(eglArray, EGL14Constants.EGL_BLUE_SIZE, wantedConfig.getBlueBits());
        setConfig(eglArray, EGL14Constants.EGL_ALPHA_SIZE, wantedConfig.getAlphaBits());
        setConfig(eglArray, EGL14Constants.EGL_DEPTH_SIZE, wantedConfig.getDepthBits());
        setConfig(eglArray, EGL14Constants.EGL_RENDERABLE_TYPE, EGL14Constants.EGL_OPENGL_ES2_BIT);
        setConfig(eglArray, EGL14Constants.EGL_SAMPLES, wantedConfig.getSamples());
        int buffers = 0;
        if (wantedConfig.getSamples() > 1) {
            buffers = 1;
        }
        setConfig(eglArray, EGL14Constants.EGL_SAMPLE_BUFFERS, buffers);
        setConfig(eglArray, EGL14Constants.EGL_NONE, EGL14Constants.EGL_NONE);
        return getConfigArray(eglArray);
    }

    /**
     * Adds one wanted config attrib to list of configuration attributes
     * 
     * @param configs
     * @param configAttrib
     * @param value
     */
    private static void setConfig(ArrayList<int[]> configs, int configAttrib, int value) {
        configs.add(new int[] { configAttrib, value });
    }

    /**
     * Returns the wanted configuration as int array.
     * 
     * @param configs
     * @return
     */
    private static int[] getConfigArray(ArrayList<int[]> configs) {
        int[] result = new int[configs.size() * 2];
        int index = 0;
        for (int[] val : configs) {
            result[index++] = val[0];
            result[index++] = val[1];
        }
        return result;
    }

}
