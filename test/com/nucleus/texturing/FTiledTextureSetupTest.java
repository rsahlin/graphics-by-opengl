package com.nucleus.texturing;

import org.junit.Assert;
import org.junit.Test;

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

    private TiledTextureSetup createSetup(String[] data) {
        TiledTextureSetup setup = new TiledTextureSetup();
        setup.importData(data, 0);
        return setup;
    }

    protected void assertImportData(String[] expected, TiledTextureSetup actual) {
        int offset = super.assertImportData(expected, actual);
        Assert.assertEquals(expected[offset + TiledTextureMapping.FRAMES_X.getIndex()], Integer.toString(actual.getFramesX()));
        Assert.assertEquals(expected[offset + TiledTextureMapping.FRAMES_Y.getIndex()], Integer.toString(actual.getFramesY()));
    }

}
