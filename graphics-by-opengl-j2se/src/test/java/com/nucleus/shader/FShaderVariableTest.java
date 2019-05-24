package com.nucleus.shader;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.shader.ShaderVariable;
import com.nucleus.opengl.shader.ShaderVariable.VariableType;

public class FShaderVariableTest {

    private final static String VARIABLE_NAME = "shadername";
    private final static int VARIABLE_SIZE = 1;
    private final static int VARIABLE_TYPE = GLES20.GL_FLOAT;
    private final static int VARIABLE_LOCATION = 42;
    private final static int VARIABLE_ACTIVE_INDEX = 5;

    int[] variableData = new int[] { VARIABLE_SIZE, VARIABLE_TYPE, VARIABLE_NAME.length(), VARIABLE_ACTIVE_INDEX };

    @Test
    public void testCreate() {

        ShaderVariable variable = new ShaderVariable(VariableType.ATTRIBUTE, VARIABLE_NAME, variableData,
                ShaderVariable.SIZE_OFFSET);
        Assert.assertTrue(VARIABLE_NAME == variable.getName());
        Assert.assertTrue(VARIABLE_SIZE == variable.getSize());
        Assert.assertTrue(VARIABLE_TYPE == variable.getDataType());
        Assert.assertTrue(VARIABLE_ACTIVE_INDEX == variable.getActiveIndex());
    }

    @Test
    public void testSetLocation() {

        ShaderVariable variable = new ShaderVariable(VariableType.ATTRIBUTE, VARIABLE_NAME, variableData,
                ShaderVariable.SIZE_OFFSET);
        variable.setLocation(VARIABLE_LOCATION);
        Assert.assertTrue(VARIABLE_LOCATION == variable.getLocation());

    }
}
