package com.nucleus.opengl.shader;

public class ShaderProgramException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 3029048705715207862L;

    public ShaderProgramException(Exception e) {
        super(e);
    }

    public ShaderProgramException(String reason) {
        super(reason);
    }

}
