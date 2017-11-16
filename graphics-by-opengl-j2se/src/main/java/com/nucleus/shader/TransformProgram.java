package com.nucleus.shader;

import com.nucleus.assets.AssetManager;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;

/**
 * Program for transformed vertices, shader calculates vertex position with position offset, rotation and scale
 * Can be used to draw lines, polygons or similar
 */
public class TransformProgram extends ShaderProgram {

    /**
     * Name of this shader - TODO where should this be defined?
     */
    protected static final String CATEGORY = "transform";
    
    protected TransformProgram(Pass pass, Texture2D.Shading shading, String category) {
        super(pass, shading, category, ShaderVariables.values());
    }
    
    @Override
    protected void setShaderSource() {
        vertexShaderName = PROGRAM_DIRECTORY + CATEGORY + VERTEX_TYPE + SHADER_SOURCE_SUFFIX;
        fragmentShaderName = PROGRAM_DIRECTORY + CATEGORY + FRAGMENT_TYPE + SHADER_SOURCE_SUFFIX;
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
            case SHADOW1:
                ShadowPass1Program program = new ShadowPass1Program(shading, CATEGORY);
                return AssetManager.getInstance().getProgram(renderer, program);
                default:
            throw new IllegalArgumentException("Invalid pass " + pass);
        }
    }


}
