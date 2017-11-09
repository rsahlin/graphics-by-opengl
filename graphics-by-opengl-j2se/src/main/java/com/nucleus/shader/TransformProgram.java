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
    protected static final String NAME = "transform";
    
    protected TransformProgram(Texture2D.Shading shading, VariableMapping[] mapping) {
        super(shading, mapping);
    }
    
    @Override
    protected void setShaderSource(Texture2D.Shading shading) {
        vertexShaderName = PROGRAM_DIRECTORY + NAME + VERTEX + SHADER_SOURCE_SUFFIX;
        fragmentShaderName = PROGRAM_DIRECTORY + NAME + FRAGMENT + SHADER_SOURCE_SUFFIX;
    }
    
    @Override
    public void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, float[] projectionMatrix, Mesh mesh)
            throws GLException {
        super.bindUniforms(gles, modelviewMatrix, projectionMatrix, mesh);
        setUniforms(gles, sourceUniforms);
        
    }

    @Override
    public ShaderProgram getProgram(NucleusRenderer renderer, Pass pass, Shading shading) {
        switch (pass) {
            case UNDEFINED:
            case ALL:
            case MAIN:
                return this;
            case SHADOW:
                return AssetManager.getInstance().getProgram(renderer, new ShadowPass1Program(shading));
                default:
            throw new IllegalArgumentException("Invalid pass " + pass);
        }
    }


}
