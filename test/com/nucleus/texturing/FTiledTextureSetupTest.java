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
        TiledTextureSetup setup = createSetup(data);
        assertImportData(data, setup);
    }

    @Override
    @Test
    public void testExportDataAsString() {
        String[] data = DataSerializeUtils.createDefaultData(TextureMapping.values(), TiledTextureMapping.values());
        TiledTextureSetup setup = createSetup(data);
        String[] result = StringUtils.getStringArray(setup.exportDataAsString());
        assertExportData(setup, result);
    }

    private TiledTextureSetup createSetup(String[] data) {
        TiledTextureSetup setup = new TiledTextureSetup();
        setup.importData(data, 0);
        return setup;
    }

    protected int assertImportData(String[] expected, TiledTextureSetup actual) {
        int offset = super.assertImportData(expected, actual);
        Assert.assertEquals(expected[offset + TiledTextureMapping.FRAMES_X.getIndex()],
                Integer.toString(actual.getFramesX()));
        Assert.assertEquals(expected[offset + TiledTextureMapping.FRAMES_Y.getIndex()],
                Integer.toString(actual.getFramesY()));
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
        DataSerializeUtils.assertDataAsString(expected.getFramesX(), actual,
                TiledTextureSetup.TiledTextureMapping.FRAMES_X, offset);
        DataSerializeUtils.assertDataAsString(expected.getFramesY(), actual,
                TiledTextureSetup.TiledTextureMapping.FRAMES_Y, offset);
        return offset + TiledTextureMapping.values().length;
    }

}
