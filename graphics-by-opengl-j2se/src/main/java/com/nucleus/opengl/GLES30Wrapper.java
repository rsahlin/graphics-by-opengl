package com.nucleus.opengl;

import java.io.IOException;
import java.io.InputStream;

import com.nucleus.io.StreamUtils;

/**
 * Wrapper for GLES30
 *
 */
public abstract class GLES30Wrapper extends GLES20Wrapper {

    public static String SHADING_LANGUAGE_300 = "300";

    @Override
    public String getShaderVersion() {
        return SHADING_LANGUAGE_300;
    }

    @Override
    public String getVersionedShaderSource(InputStream shaderStream) throws IOException {
        return new String(StreamUtils.readFromStream(shaderStream));
    }

}
