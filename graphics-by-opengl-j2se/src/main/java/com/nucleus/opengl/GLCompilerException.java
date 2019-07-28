package com.nucleus.opengl;

import com.nucleus.shader.ShaderBinary;

/**
 * If there is an exception compiling a GL shader, ie call to check compile status returns an status error code.
 * glGetShader(GL_COMPILE_STATUS) != GL_TRUE
 *
 */
public class GLCompilerException extends GLException {

    public final int status;
    public final int shader;
    public final ShaderBinary source;
    public final String shaderInfoLog;

    public GLCompilerException(int status, int shader, ShaderBinary source, String shaderInfoLog) {
        super("Shader compilation of " + source.getFullSourceName() +
                "failed with status " + status + " for shader " + shader + "\n" + shaderInfoLog);
        this.status = status;
        this.shader = shader;
        this.shaderInfoLog = shaderInfoLog;
        this.source = source;
    }

}
