package com.nucleus.texturing;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.common.StringUtils;
import com.nucleus.texturing.TextureSetup.TextureMapping;
import com.nucleus.texturing.TiledTextureSetup.TiledTextureMapping;
import com.nucleus.utils.DataSerializeUtils;

public class FTiledTextureSetupTest extends FTextureSetupTest {

    @Override
    @Test
    public void testImportData() {
        String[] data = DataSerializeUtils.createDefaultData(TextureMapping.values(), TiledTextureMapping.values());
        TiledTextureSetup setup = (TiledTextureSetup) DataSerializeUtils.createSetup(data, new TiledTextureSetup());
        assertImportData(data, setup);
    }

    @Override
    @Test
    public void testExportDataAsString() {
        String[] data = DataSerializeUtils.createDefaultData(TextureMapping.values(), TiledTextureMapping.values());
        TiledTextureSetup setup = (TiledTextureSetup) DataSerializeUtils.createSetup(data, new TiledTextureSetup());
        String[] result = StringUtils.getStringArray(setup.exportDataAsString());
        assertExportData(setup, result);
    }

    protected int assertImportData(String[] expected, TiledTextureSetup actual) {
        int offset = super.assertImportData(expected, actual);
        Assert.assertEquals(expected.length, TiledTextureMapping.values().length + offset);
        DataSerializeUtils.assertString(expected, TiledTextureMapping.FRAMES_X, actual.getFramesX(), offset);
        DataSerializeUtils.assertString(expected, TiledTextureMapping.FRAMES_Y, actual.getFramesY(), offset);
        return offset + TiledTextureMapping.values().length;
    }

    /**
     * Asserts the String data as got from export data as string.
     * 
     * @param expected
     * @param actual
     */
    protected int assertExportData(TiledTextureSetup expected, String[] actual) {
        int offset = super.assertExportData(expected, actual);
        Assert.assertEquals(actual.length, TiledTextureMapping.values().length + offset);
        DataSerializeUtils.assertDataAsString(expected.getFramesX(), actual,
                TiledTextureSetup.TiledTextureMapping.FRAMES_X, offset);
        DataSerializeUtils.assertDataAsString(expected.getFramesY(), actual,
                TiledTextureSetup.TiledTextureMapping.FRAMES_Y, offset);
        return offset + TiledTextureMapping.values().length;
    }

}
