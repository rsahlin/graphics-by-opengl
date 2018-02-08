package com.nucleus.shader;

import com.nucleus.assets.AssetManager;
import com.nucleus.geometry.Mesh;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;

/**
 * Program for transformed vertices, shader calculates vertex position with position offset, rotation and scale
 * Can be used to draw lines, polygons or similar
 * 
 * TODO - If this is not used then remove
 * 
 */
public class TransformProgram extends ShaderProgram {

    /**
     * Name of this shader - TODO where should this be defined?
     */
    protected static final String CATEGORY = "transform";

    protected TransformProgram(Pass pass, Texture2D.Shading shading, String category) {
        super(pass, shading, category, ShaderVariables.values(), Shaders.VERTEX_FRAGMENT);
    }

    @Override
    public ShaderProgram getProgram(NucleusRenderer renderer, Pass pass, Shading shading) {
        switch (pass) {
            case UNDEFINED:
            case ALL:
            case MAIN:
                return this;
            case SHADOW1:
                ShadowPass1Program program = new ShadowPass1Program(this, shading, CATEGORY);
                return AssetManager.getInstance().getProgram(renderer, program);
            default:
                throw new IllegalArgumentException("Invalid pass " + pass);
        }
    }

    @Override
    public void setUniformData(float[] uniforms, Mesh mesh) {
        // TODO Auto-generated method stub

    }

}
