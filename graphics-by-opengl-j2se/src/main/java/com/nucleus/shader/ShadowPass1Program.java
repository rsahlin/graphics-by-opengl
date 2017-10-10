package com.nucleus.shader;

import com.nucleus.geometry.AttributeUpdater.Property;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;

public class ShadowPass1Program extends ShaderProgram {

    public ShadowPass1Program() {
        super(null);
    }
    
    @Override
    public VariableMapping getVariableMapping(ShaderVariable variable) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setupUniforms(Mesh mesh) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, float[] projectionMatrix, Mesh mesh)
            throws GLException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getVariableCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getPropertyOffset(Property property) {
        // TODO Auto-generated method stub
        return 0;
    }

}
