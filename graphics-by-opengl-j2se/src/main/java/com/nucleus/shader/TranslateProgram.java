package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;

/**
 * Program for translated vertices, shader calculates vertex position with position offset
 * Can be used to draw lines, polygons or similar - objects cannot be independently rotated or scaled
 * - use {@link TransformProgram} 
 */
public class TranslateProgram extends ShaderProgram {

    public TranslateProgram(Texture2D.Shading shading) {
        super(shading, ShaderVariables.values());
    }

    @Override
    public void bindUniforms(GLES20Wrapper gles, float[][] matrices, Mesh mesh)
            throws GLException {
        super.bindUniforms(gles, matrices, mesh);
        setUniforms(gles, sourceUniforms);
    }


    @Override
    public ShaderProgram getProgram(NucleusRenderer renderer, Pass pass, Shading shading) {
        switch (pass) {
            case UNDEFINED:
            case ALL:
            case MAIN:
                return this;
                default:
            throw new IllegalArgumentException("Invalid pass " + pass);
        }
    }


}
