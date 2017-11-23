package com.nucleus.egl;

public class EGLUtils {

    private static final int[] EGL_SURFACE_TYPES = new int[] { EGL14Constants.EGL_PBUFFER_BIT, EGL14Constants.EGL_PIXMAP_BIT,
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

}
