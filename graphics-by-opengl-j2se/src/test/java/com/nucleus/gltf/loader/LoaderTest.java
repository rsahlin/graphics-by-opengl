package com.nucleus.gltf.loader;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.Asset;
import com.nucleus.scene.gltf.Buffer;
import com.nucleus.scene.gltf.BufferView;
import com.nucleus.scene.gltf.BufferView.Target;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.Loader;
import com.nucleus.scene.gltf.Material;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.scene.gltf.Node;
import com.nucleus.scene.gltf.Scene;

public class LoaderTest extends BaseTestCase {

    @Test
    public void loadglTFAssetBox() throws IOException, URISyntaxException, GLTFException {

        // Use known scene to validate that values are not null
        GLTF asset = Loader.loadAsset("Box/glTF/", "Box.gltf");

        Assert.assertNotNull(asset);
        checkAsset(asset.getAsset());
        checkScene(asset.getScenes()[0]);
        checkNode(asset.getNodes()[0]);
        checkMesh(asset.getMeshes()[0]);
        checkBuffer(asset, asset.getBuffers()[0]);
        checkMaterial(asset.getMaterials()[0]);
        checkAccessor(asset.getAccessors()[0]);

    }

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
        Assert.assertNotNull(Target.getTarget(bufferView.getTarget()));
    }

    protected void checkMaterial(Material material) {
        Assert.assertNotNull(material.getPbrMetallicRoughness());
    }

}
