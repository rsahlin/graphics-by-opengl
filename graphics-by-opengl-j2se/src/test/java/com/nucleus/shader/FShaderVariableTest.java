package com.nucleus.shader;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.shader.ShaderVariable.VariableType;

public class FShaderVariableTest {

    private final static String VARIABLE_NAME = "shadername";
    private final static int VARIABLE_SIZE = 1;
    private final static int VARIABLE_TYPE = GLES20.GL_FLOAT;
    private final static int VARIABLE_LOCATION = 42;

    int[] variableData = new int[] { VARIABLE_NAME.length(), VARIABLE_SIZE, VARIABLE_TYPE };

    @Test
    public void testCreate() {

        ShaderVariable variable = new ShaderVariable(VariableType.ATTRIBUTE, VARIABLE_NAME, variableData,
                ShaderVariable.SIZE_OFFSET);
        Assert.assertEquals(VARIABLE_NAME, variable.getName());
        Assert.assertEquals(VARIABLE_SIZE, variable.getSize());
        Assert.assertEquals(VARIABLE_TYPE, variable.getDataType());

    }

    @Test
    public void testSetLocation() {

        ShaderVariable variable = new ShaderVariable(VariableType.ATTRIBUTE, VARIABLE_NAME, variableData,
                ShaderVariable.SIZE_OFFSET);
        variable.setLocation(VARIABLE_LOCATION);
        Assert.assertEquals(VARIABLE_LOCATION, variable.getLocation());

    }
}
