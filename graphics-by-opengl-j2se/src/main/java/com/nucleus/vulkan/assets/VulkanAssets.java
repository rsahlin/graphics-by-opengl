package com.nucleus.vulkan.assets;

import java.io.IOException;

import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.assets.BaseAssets;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.Image;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.ImageFactory;
import com.nucleus.texturing.Texture2D;

/**
 * Implementation of Assets interface for Vulkan - fetch from {@link NucleusRenderer#getAssets()}
 * Loading and unloading assets, mainly textures - this is the main entrypoint for loading of textures
 * and programs.
 * Clients shall only use this class - do not call methods to load assets (program/texture etc) separately.
 *
 */
public class VulkanAssets extends BaseAssets {

    @Override
    public BufferImage[] loadTextureMIPMAP(ImageFactory imageFactory, Texture2D texture) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void loadGLTFAssets(NucleusRenderer renderer, GLTF glTF) throws IOException, BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public GLTF getGLTFAsset(String fileName) throws IOException, GLTFException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteGLTFAssets(NucleusRenderer renderer, GLTF gltf) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy(NucleusRenderer renderer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createTexture(Texture2D texture, int target) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteTexture(Image image) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteTextures(Texture2D[] textures) {
        // TODO Auto-generated method stub

    }

    @Override
    protected int[] createTextureName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected GraphicsPipeline<?> createGraphicsPipeline(NucleusRenderer renderer, GraphicsShader shader)
            throws BackendException {
        // TODO Auto-generated method stub
        return null;
    }

}
