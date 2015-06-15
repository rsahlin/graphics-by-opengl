package com.nucleus.texturing;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.common.StringUtils;
import com.nucleus.texturing.TextureSetup.TextureMapping;
import com.nucleus.utils.DataSerializeUtils;

public class FTextureSetupTest {

    @Test
    public void testImportData() {

        String[] data = DataSerializeUtils.createDefaultData(TextureMapping.values());
        TextureSetup setup = createSetup(data);
        assertImportData(data, setup);
    }

    @Test
    public void testExportDataAsString() {
        String[] data = DataSerializeUtils.createDefaultData(TextureMapping.values());
        TextureSetup setup = createSetup(data);

        String[] result = StringUtils.getStringArray(setup.exportDataAsString());
        assertExportData(setup, result);

    }

    private TextureSetup createSetup(String[] data) {
        TextureSetup setup = new TextureSetup();
        setup.importData(data, 0);
        return setup;
    }

    /**
     * Asserts the String data as got from export data as string.
     * 
     * @param expected
     * @param actual
     * @param Number of values asserted
     */
    protected int assertExportData(TextureSetup expected, String[] actual) {
        DataSerializeUtils.assertDataAsString(expected.getSourceName(), actual, TextureMapping.SOURCENAME);
        DataSerializeUtils.assertDataAsString(expected.getResolution(), actual, TextureMapping.TARGET_RESOLUTION);
        DataSerializeUtils.assertDataAsString(expected.getLevels(), actual, TextureMapping.LEVELS);
        return TextureMapping.values().length;
    }

    /**
     * Asserts the setup class as got from import data
     * 
     * @param expected
     * @param actual
     * @return number of values asserted
     */
    protected int assertImportData(String[] expected, TextureSetup actual) {
        Assert.assertEquals(expected[TextureMapping.SOURCENAME.getIndex()], actual.getSourceName());
        Assert.assertEquals(expected[TextureMapping.TARGET_RESOLUTION.getIndex()], actual.getResolution().toString());
        Assert.assertEquals(expected[TextureMapping.LEVELS.getIndex()], Integer.toString(actual.getLevels()));
        return TextureMapping.values().length;
    }

}
