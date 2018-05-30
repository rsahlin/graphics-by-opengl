package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.texturing.TextureType;

/**
 * Program for translated vertices, shader calculates vertex position with position offset
 * Can be used to draw objects that cannot be independently rotated or scaled, for instance a quad.
 */
public class TranslateProgram extends ShaderProgram {

    public TranslateProgram(Texture2D texture) {
        super(null,
                (texture == null || texture.textureType == TextureType.Untextured) ? Shading.flat : Shading.textured,
                null, CommonShaderVariables.values(), ProgramType.VERTEX_FRAGMENT);
    }

    public TranslateProgram(Texture2D.Shading shading) {
        super(null, shading, null, CommonShaderVariables.values(), ProgramType.VERTEX_FRAGMENT);
    }

    @Override
    public ShaderProgram getProgram(GLES20Wrapper gles, Pass pass, Shading shading) {
        switch (pass) {
            case UNDEFINED:
            case ALL:
            case MAIN:
                return this;
            default:
                throw new IllegalArgumentException("Invalid pass " + pass);
        }
    }

    @Override
    public void updateUniformData(float[] destinationUniform, Mesh mesh) {
    }

    @Override
    public void initBuffers(Mesh mesh) {
    }

}
