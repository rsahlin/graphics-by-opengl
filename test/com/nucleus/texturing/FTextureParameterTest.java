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
        int[] values = actual.values;
        Assert.assertEquals(expected[TextureParameterMapping.MAG_FILTER.getIndex()],
                TextureParameter.valueToString(values[TextureParameter.MAG_FILTER]));
        Assert.assertEquals(expected[TextureParameterMapping.MIN_FILTER.getIndex()],
                TextureParameter.valueToString(values[TextureParameter.MIN_FILTER]));
        Assert.assertEquals(expected[TextureParameterMapping.WRAP_S.getIndex()],
                TextureParameter.valueToString(values[TextureParameter.WRAP_S]));
        Assert.assertEquals(expected[TextureParameterMapping.WRAP_T.getIndex()],
                TextureParameter.valueToString(values[TextureParameter.WRAP_T]));
        return TextureMapping.values().length;
    }
}
