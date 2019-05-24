package com.nucleus.opengl;

import com.nucleus.opengl.shader.ShaderSource;

/**
 * If there is an exception compiling a GL shader, ie call to check compile status returns an status error code.
 * glGetShader(GL_COMPILE_STATUS) != GL_TRUE
 *
 */
public class GLCompilerException extends GLException {

    public final int status;
    public final int shader;
    public final ShaderSource source;
    public final String shaderInfoLog;

    public GLCompilerException(int status, int shader, ShaderSource source, String shaderInfoLog) {
        super("Shader compilation of " + source.getFullSourceName() +
                "failed with status " + status + " for shader " + shader + "\n" + shaderInfoLog);
        this.status = status;
        this.shader = shader;
        this.shaderInfoLog = shaderInfoLog;
        this.source = source;
    }

}
