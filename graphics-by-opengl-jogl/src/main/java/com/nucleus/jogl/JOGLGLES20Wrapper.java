package com.nucleus.jogl;

import java.nio.Buffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL2ES2;
import com.nucleus.opengl.GLES20Wrapper;

public class JOGLGLES20Wrapper extends GLES20Wrapper {

    private final static String GLES_NULL = "GLES20 is null";

    GL2ES2 gles;

    /**
     * Creates a new instance of the GLES20 wrapper for JOGL
     * 
     * @param gles The JOGL GLES20 instance
     * @throws IllegalArgumentException If gles is null
     */
    public JOGLGLES20Wrapper(GL2ES2 gles) {
        if (gles == null) {
            throw new IllegalArgumentException(GLES_NULL);
        }
        this.gles = gles;
    }

    @Override
    public void glAttachShader(int program, int shader) {
        gles.glAttachShader(program, shader);
    }

    @Override
    public void glLinkProgram(int program) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glShaderSource(int shader, String shaderSource) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glCompileShader(int shader) {
        // TODO Auto-generated method stub

    }

    @Override
    public int glCreateShader(int type) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int glCreateProgram() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glUseProgram(int program) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetProgramiv(int program, int pname, int[] params, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetActiveAttrib(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetActiveUniform(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
        // TODO Auto-generated method stub

    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int glGetAttribLocation(int program, String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int glGetError() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, Buffer ptr) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] v, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBindAttribLocation(int program, int index, String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public String glGetShaderInfoLog(int shader) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String glGetProgramInfoLog(int program) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void glGenTextures(int count, int[] textures, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glActiveTexture(int texture) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBindTexture(int target, int texture) {
        // TODO Auto-generated method stub

    }

    @Override
    public String glGetString(int name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void glGetIntegerv(int pname, int[] params, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glClear(int mask) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDisable(int cap) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glEnable(int cap) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, Buffer pixels) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        // TODO Auto-generated method stub

    }

}
