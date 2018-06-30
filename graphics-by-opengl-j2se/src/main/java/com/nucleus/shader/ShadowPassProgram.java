package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;

public abstract class ShadowPassProgram extends ShaderProgram {

    /**
     * The program that should be used to render the object casting shadow
     */
    protected ShaderProgram objectProgram;

    /**
     * TODO Look into the shader programs using this constructor - maybe they can be unified?
     * 
     * @param objectProgram The program for rendering the object casting shadow
     * @param shading
     * @param category
     * @param shaders
     */
    public ShadowPassProgram(ShaderProgram objectProgram, Pass pass, Texture2D.Shading shading, String category,
            ProgramType shaders) {
        super(pass, shading, category, shaders);
        setIndexer(objectProgram.variableIndexer);
        this.objectProgram = objectProgram;
    }

    @Override
    public ShaderProgram getProgram(GLES20Wrapper gles, Pass pass, Shading shading) {
        throw new IllegalArgumentException("Not valid");
    }

    @Override
    public void updateAttributes(GLES20Wrapper gles, Mesh mesh) throws GLException {
        objectProgram.updateAttributes(gles, mesh);
    }

    @Override
    public void updateUniforms(GLES20Wrapper gles, float[][] matrices, Mesh mesh)
            throws GLException {
        /**
         * Currently calls ShaderProgram#setUniformData() in order to set necessary data from the program int
         * uniform storage.
         * This could potentially break the shadow program if needed uniform data is set in some other method.
         * TODO - Make sure that the interface declares and mandates that uniform data shall be set in #setUniformData()
         */
        objectProgram.updateUniformData(uniforms, mesh);
        super.updateUniforms(gles, matrices, mesh);
    }

}
