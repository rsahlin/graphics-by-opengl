package com.nucleus.shader;

import com.nucleus.geometry.AttributeUpdater.Property;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;

/**
 * Program for transformed vertices, shader calculates vertex position with position offset, rotation and scale
 * Can be used to draw lines, polygons or similar
 */
public class VertexTransformProgram extends ShaderProgram {

    public VertexTransformProgram() {
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
