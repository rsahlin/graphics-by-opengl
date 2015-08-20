package com.nucleus.texturing;

import org.junit.Test;

import com.nucleus.common.StringUtils;
import com.nucleus.texturing.TextureSetup.TextureMapping;
import com.nucleus.utils.DataSerializeUtils;

public class FTextureSetupTest {

    private final static String[] data = DataSerializeUtils.createDefaultData(TextureMapping.values());

    @Test
    public void testImportData() {
        TextureSetup setup = (TextureSetup) DataSerializeUtils.createSetup(data, new TextureSetup());
        assertImportData(data, setup);
    }

    @Test
    public void testExportDataAsString() {
        TextureSetup setup = (TextureSetup) DataSerializeUtils.createSetup(data, new TextureSetup());
        String[] result = StringUtils.getStringArray(setup.exportDataAsString());
        assertExportData(setup, result);

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
        DataSerializeUtils.assertString(expected, TextureMapping.SOURCENAME, actual.getSourceName(), 0);
        DataSerializeUtils.assertString(expected, TextureMapping.TARGET_RESOLUTION, actual.getResolution().name(), 0);
        DataSerializeUtils.assertString(expected, TextureMapping.LEVELS, actual.getLevels(), 0);
        return TextureMapping.values().length;
    }

}
