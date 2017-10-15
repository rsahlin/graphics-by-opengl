package com.nucleus.shader;

import com.nucleus.geometry.AttributeUpdater.Property;
import com.nucleus.SimpleLogger;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.vecmath.Matrix;

/**
 * Program for transformed vertices, shader calculates vertex position with position offset, rotation and scale
 * Can be used to draw lines, polygons or similar
 */
public class TransformProgram extends ShaderProgram {

    /**
     * Name of this shader - TODO where should this be defined?
     */
    protected static final String NAME = "Transform";
    
    public TransformProgram() {
        super(ShaderVariables.values());
        vertexShaderName = PROGRAM_DIRECTORY + NAME + VERTEX + SHADER_SOURCE_SUFFIX;
        fragmentShaderName = PROGRAM_DIRECTORY + NAME + FRAGMENT + SHADER_SOURCE_SUFFIX;
    }
    
    @Override
    public void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, float[] projectionMatrix, Mesh mesh)
            throws GLException {
        // Refresh the uniform matrix
        // TODO prefetch the offsets for the shader variables and store in array.
        System.arraycopy(modelviewMatrix, 0, mesh.getUniforms(), shaderVariables[ShaderVariables.uMVMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        System.arraycopy(projectionMatrix, 0, mesh.getUniforms(),
                shaderVariables[ShaderVariables.uProjectionMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        bindUniforms(gles, uniforms, mesh.getUniforms());
        
    }

    @Override
    public int getPropertyOffset(Property property) {
        ShaderVariable v = null;
        switch (property) {
        case TRANSLATE:
            v = shaderVariables[ShaderVariables.aTranslate.index];
            break;
        case ROTATE:
            v = shaderVariables[ShaderVariables.aRotate.index];
            break;
        case SCALE:
            v = shaderVariables[ShaderVariables.aScale.index];
            break;
        default:
        }
        if (v != null) {
            return v.getOffset();
        } else {
            SimpleLogger.d(getClass(), "No ShaderVariable for " + property);
            
        }
        return -1;
    }

}
