package com.nucleus.jogl;

import java.util.ArrayList;

import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLES20Wrapper;

/**
 * Utils for JOGL
 *
 */
public class JOGLGLESUtils {

    /**
     * Not used buffer object names
     */
    private static ArrayList<int[]> bufferNames = new ArrayList<int[]>();
    /**
     * Used buffer object names
     */
    private static ArrayList<int[]> usedBufferNames = new ArrayList<int[]>();

    /**
     * Gets an unused buffer object name, if one does not exist it is allocated.
     * 
     * @return
     */
    protected static int[] getName(GLES20Wrapper gles) {
        if (bufferNames.size() > 0) {
            int[] used = bufferNames.remove(bufferNames.size() - 1);
            usedBufferNames.add(used);
            return used;
        } else {
            int[] names = new int[1];
            gles.glGenBuffers(names);
            usedBufferNames.add(names);
            SimpleLogger.d(JOGLGLESUtils.class, "Allocated 1 buffer object name: " + names[0]);
            return names;
        }
    }

    /**
     * Moves all used buffer names to the unused buffer name list.
     */
    protected static void freeNames() {
        while (!usedBufferNames.isEmpty()) {
            bufferNames.add(usedBufferNames.remove(usedBufferNames.size() - 1));
        }
    }

}
