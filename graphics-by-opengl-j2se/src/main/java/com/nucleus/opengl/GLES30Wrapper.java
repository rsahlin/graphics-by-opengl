package com.nucleus.opengl;

import java.util.StringTokenizer;

/**
 * Wrapper for GLES30
 *
 */
public abstract class GLES30Wrapper extends GLES20Wrapper {

    public static String SHADING_LANGUAGE_300 = "300";

    private final static String[] GLES3_VERTEX_REPLACEMENTS = new String[] { "attribute", "in", "varying", "out" };
    private final static String[] GLES3_FRAGMENT_REPLACEMENTS = new String[] { "varying", "in" };

    @Override
    public String getShaderVersion() {
        return SHADING_LANGUAGE_300 + " " + ES;
    }

    private String replaceGLES20(String source, int type) {
        StringTokenizer st = new StringTokenizer(source, "\n");
        StringBuffer result = new StringBuffer();
        String t = "";
        String[] replacements = null;
        switch (type) {
            case GLES20.GL_VERTEX_SHADER:
                replacements = GLES3_VERTEX_REPLACEMENTS;
                break;
            case GLES20.GL_FRAGMENT_SHADER:
                replacements = GLES3_FRAGMENT_REPLACEMENTS;
                break;
            default:
                throw new IllegalArgumentException("Invalid shader type: " + type);

        }
        while (st.hasMoreTokens()) {
            t = st.nextToken().trim();
            for (int i = 0; i < replacements.length; i += 2) {
                if (t.startsWith(replacements[i])) {
                    t = replacements[i + 1] + t.substring(replacements[i].length());
                }
            }
            result.append(t + "\n");
        }
        return result.toString();
    }

    /**
     * Abstraction for glSamplerParameteri( GLuint sampler, GLenum pname, GLint param);
     * 
     * @param sampler
     * @param pname
     * @param param
     */
    public abstract void glSamplerParameteri(int sampler, int pname, int param);

    /**
     * Abstraction for glBindBufferBase(GLenum target, GLuint index, GLuint buffer);
     * 
     * @param target
     * @param index
     * @param buffer
     */
    public abstract void glBindBufferBase(int target, int index, int buffer);

    /**
     * Abstraction for glUniformBlockBinding(GLuint program, GLuint uniformBlockIndex, GLuint uniformBlockBinding);
     * 
     * @param program
     * @param uniformBlockIndex
     * @param uniformBlockBinding
     */
    public abstract void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding);

    /**
     * Abstraction for glBindBufferRange( GLenumtarget, GLuintindex, GLuintbuffer, GLintptroffset, GLsizeiptrsize);
     * 
     * @param target
     * @param index
     * @param buffer
     * @param ptroffset
     * @param ptrsize
     */
    public abstract void glBindBufferRange(int target, int index, int buffer, int ptroffset, int ptrsize);

}
