package com.nucleus.opengl;

import java.util.ArrayList;
import java.util.List;

import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLESWrapper.Error;

/**
 * Platform agnostic GL utility methods, uses the GLES20Wrapper for GLES functions.
 * 
 * @author Richard Sahlin
 *
 */
public class GLUtils {

    private static boolean throwErrors = false;

    /**
     * Checks for OpenGL error, if there is at least one error then all errors are fetched and GLException is thrown.
     * Call this method after all GL methods that can raise an error where the error means failure.
     * When this method returns there will be zero reported errors from GL.
     * 
     * @param ga The tag to display with errors
     * @return true if GL error but exception not thrown
     * @throws GLException If there is one or more errors in GL.
     */
    public static boolean handleError(GLES20Wrapper gles, String tag) throws GLException {
        List<Integer> errors = getErrors(gles);
        if (errors != null) {
            if (throwErrors) {
                throw new GLException(tag, errors);
            } else {
                for (Integer i : errors) {
                    SimpleLogger.d(tag,
                            "GLError: " + Error.getError(i) + " : value " + i);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if there is one or more errors and returns the GL error codes in a list.
     * 
     * @return List of one or more GL error codes or null if no error.
     */
    public static List<Integer> getErrors(GLES20Wrapper gles) {
        List<Integer> errors = null;
        int e = 0;
        while ((e = gles.glGetError()) != 0) {
            if (errors == null) {
                errors = new ArrayList<Integer>();
            }
            errors.add(e);
        }
        return errors;
    }

}
