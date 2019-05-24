package com.nucleus.opengl.shader;

import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.Pass;

public abstract class ShadowPassProgram extends ShaderProgram {

    /**
     * The program that should be used to render the object casting shadow
     */
    protected ShaderProgram objectProgram;

    /**
     * TODO Look into the shader programs using this constructor - maybe they can be unified?
     * 
     * @param objectProgram The program for rendering the object casting shadow
     * @param categorizer
     * @param shaders
     */
    public ShadowPassProgram(ShaderProgram objectProgram, Categorizer categorizer, ShaderProgram.ProgramType shaders) {
        super(categorizer, shaders);
        setIndexer(objectProgram.variableIndexer);
        this.objectProgram = objectProgram;
    }

    @Override
    public ShaderProgram getProgram(GLES20Wrapper gles, Pass pass, ShaderProgram.Shading shading) {
        throw new IllegalArgumentException("Not valid");
    }

    @Override
    public void updateAttributes(GLES20Wrapper gles, AttributeUpdater mesh) throws GLException {
        objectProgram.updateAttributes(gles, mesh);
    }

    @Override
    public void uploadUniforms(GLES20Wrapper gles) throws GLException {
        /**
         * Currently calls ShaderProgram#setUniformData() in order to set necessary data from the program int
         * uniform storage.
         * This could potentially break the shadow program if needed uniform data is set in some other method.
         * TODO - Make sure that the interface declares and mandates that uniform data shall be set in #setUniformData()
         */
        objectProgram.updateUniformData(uniforms);
        super.uploadUniforms(gles);
    }

    @Override
    protected String getShaderSourceName(ShaderType type) {
        /**
         * Shadow programs may need to call the objectProgram to get the sources, this is known if categorizer returns
         * null.
         * returns null.
         */
        String name = function.getShaderSourceName(type);
        if (name == null) {
            name = objectProgram.getShaderSourceName(type);
        }
        return name;
    }

}
