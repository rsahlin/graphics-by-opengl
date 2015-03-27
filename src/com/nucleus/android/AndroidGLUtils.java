package com.nucleus.android;

import java.util.ArrayList;
import java.util.List;

import android.opengl.GLES20;

import com.nucleus.opengl.GLException;

/**
 * Opengl GL utilities that use Android implementation of OpenGL
 * 
 * @author Richard Sahlin
 *
 */
public class AndroidGLUtils {

    /**
     * Checks for OpenGL error, if there is at least one error then all errors are fetched and GLException is thrown.
     * Call this method after all GL methods that can raise an error where the error means failure.
     * When this method returns there will be zero reported errors from GL.
     * 
     * @param detailMessage The detailed exception message if there is a GL error.
     * @throws GLException If there is one or more errors in GL.
     */
    public static void handleError(String detailMessage) throws GLException {
        List<Integer> errors = getErrors();
        if (errors != null) {
            throw new GLException(detailMessage, errors);
        }
    }

    /**
     * Checks if there is one or more errors and returns the GL error codes in a list.
     * 
     * @return List of one or more GL error codes or null if no error.
     */
    public static List<Integer> getErrors() {
        List<Integer> errors = null;
        int e = 0;
        while ((e = GLES20.glGetError()) != 0) {
            if (errors == null) {
                errors = new ArrayList<Integer>();
            }
            errors.add(e);
        }
        return errors;
    }

}
