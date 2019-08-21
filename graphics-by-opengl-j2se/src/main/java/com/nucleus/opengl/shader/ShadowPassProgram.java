package com.nucleus.opengl.shader;

import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.GenericShaderProgram;

public abstract class ShadowPassProgram extends GenericShaderProgram {

    public ShadowPassProgram(Pass pass, Shading shading, String category, ProgramType shaders) {
        init(null, pass, shading, category, shaders);
    }

    /**
     * The program that should be used to render the object casting shadow
     */
    protected GenericShaderProgram objectProgram;

    public void updateAttributes(GLES20Wrapper gles, AttributeUpdater mesh) throws GLException {
        // objectProgram.updateAttributes(gles, mesh);
    }

    public void uploadUniforms(GLES20Wrapper gles) throws GLException {
        /**
         * Currently calls ShaderProgram#setUniformData() in order to set necessary data from the program int
         * uniform storage.
         * This could potentially break the shadow program if needed uniform data is set in some other method.
         * TODO - Make sure that the interface declares and mandates that uniform data shall be set in #setUniformData()
         */
        objectProgram.updateUniformData();
        // super.uploadUniforms(gles);
    }

}
