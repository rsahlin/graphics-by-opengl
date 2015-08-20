package com.nucleus.texturing;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.common.StringUtils;
import com.nucleus.texturing.TextureParameter.TextureParameterMapping;
import com.nucleus.texturing.TextureSetup.TextureMapping;
import com.nucleus.utils.DataSerializeUtils;

public class FTextureParameterTest {

    @Test
    public void testImportData() {
        String[] data = DataSerializeUtils.createDefaultData(TextureParameterMapping.values());
        TextureParameter setup = createSetup(data);
        assertImportData(data, setup);
    }

    @Test
    public void testExportDataAsString() {
        String[] data = DataSerializeUtils.createDefaultData(TextureParameterMapping.values());
        TextureParameter setup = createSetup(data);

        String[] result = StringUtils.getStringArray(setup.exportDataAsString());
        assertExportData(setup, result);

    }

    private TextureParameter createSetup(String[] data) {
        TextureParameter setup = new TextureParameter();
        setup.importData(data, 0);
        return setup;
    }

    /**
     * Asserts the String data as got from export data as string.
     * 
     * @param expected
     * @param actual
     */
    protected void assertExportData(TextureParameter expected, String[] actual) {
        Assert.assertEquals(actual.length, TextureParameterMapping.values().length);
        DataSerializeUtils.assertDataAsString(expected.getValueAsString(TextureParameterMapping.MAG_FILTER), actual,
                TextureParameterMapping.MAG_FILTER);
        DataSerializeUtils.assertDataAsString(expected.getValueAsString(TextureParameterMapping.MIN_FILTER), actual,
                TextureParameterMapping.MIN_FILTER);
        DataSerializeUtils.assertDataAsString(expected.getValueAsString(TextureParameterMapping.WRAP_S), actual,
                TextureParameterMapping.WRAP_S);
        DataSerializeUtils.assertDataAsString(expected.getValueAsString(TextureParameterMapping.WRAP_T), actual,
                TextureParameterMapping.WRAP_T);
    }

    /**
     * Asserts the setup class as got from import data
     * 
     * @param expected
     * @param actual
     * @return number of values asserted
     */
    protected int assertImportData(String[] expected, TextureParameter actual) {
        Assert.assertEquals(expected.length, TextureParameterMapping.values().length);
        DataSerializeUtils.assertString(expected, TextureParameterMapping.MAG_FILTER,
                actual.getValueAsString(TextureParameterMapping.MAG_FILTER), 0);
        DataSerializeUtils.assertString(expected, TextureParameterMapping.MIN_FILTER,
                actual.getValueAsString(TextureParameterMapping.MIN_FILTER), 0);
        DataSerializeUtils.assertString(expected, TextureParameterMapping.WRAP_S,
                actual.getValueAsString(TextureParameterMapping.WRAP_S), 0);
        DataSerializeUtils.assertString(expected, TextureParameterMapping.WRAP_T,
                actual.getValueAsString(TextureParameterMapping.WRAP_T), 0);
        return TextureMapping.values().length;
    }
}
