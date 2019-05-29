package com.nucleus.scene.gltf;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.junit.Assert;

public class LoaderTest extends BaseTestCase {

    protected void checkAccessor(Accessor accessor) {
        Assert.assertNotNull(accessor.getMax());
        Assert.assertNotNull(accessor.getMin());
        Assert.assertNotNull(accessor.getType());
        Assert.assertTrue(accessor.getBufferViewIndex() >= 0);
        Assert.assertTrue(accessor.getCount() > 0);
        Assert.assertNotNull(accessor.getBufferView());
        Assert.assertNotNull(accessor.getComponentType());

    }

    protected void checkAsset(Asset asset) {
        assertNotNull(asset.getVersion());
        assertNotNull(asset.getGenerator());
    }

    protected void checkScene(Scene scene) {
        Assert.assertNotNull(scene.getNodes());
    }

    protected void checkNode(Node node) {
        Assert.assertNotNull(node.getChildren());
        Assert.assertNotNull(node.getMatrix());
    }

    protected void checkMesh(Mesh mesh) {
        HashMap<com.nucleus.scene.gltf.Primitive.Attributes, Integer> attributes = mesh.getPrimitives()[0]
                .getAttributes();
        Assert.assertNotNull(attributes);
    }

    protected void checkBuffer(GLTF glTF, Buffer buffer) throws IOException, URISyntaxException {
    }

    protected void checkBufferView(BufferView bufferView) {
        Assert.assertTrue(bufferView.getBufferIndex() >= 0);
        Assert.assertTrue(bufferView.getByteLength() > 0);
        Assert.assertNotNull(bufferView.getTarget());
    }

    protected void checkMaterial(Material material) {
        Assert.assertNotNull(material.getPbrMetallicRoughness());
        // Check default values.
        Assert.assertTrue(material.getAlphaCutoff() == Material.DEFAULT_ALPHA_CUTOFF);
        Assert.assertTrue(material.getAlphaMode() == Material.DEFAULT_ALPHA_MODE);
        Assert.assertTrue(material.getEmissiveFactor() == Material.DEFAULT_EMISSIVE_FACTOR);
        Assert.assertTrue(material.isDoubleSided() == Material.DEFAULT_DOUBLE_SIDED);
    }

}
