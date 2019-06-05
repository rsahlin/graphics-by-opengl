package com.nucleus.vulkan;

import java.nio.IntBuffer;
import java.util.ArrayList;

import com.nucleus.Backend;
import com.nucleus.Backend.DrawMode;
import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.Pipeline;
import com.nucleus.assets.Assets;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.shader.ShaderVariable;
import com.nucleus.renderer.BaseRenderer;
import com.nucleus.renderer.BufferFactory;
import com.nucleus.scene.RenderableNode;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Image;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.scene.gltf.Texture;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.Texture2D;

public class VulkanBaseRenderer extends BaseRenderer {

    protected Vulkan10Wrapper vulkan;

    public VulkanBaseRenderer(Backend backend) {
        super(backend);
        if (!(backend instanceof Vulkan10Wrapper)) {
            throw new IllegalArgumentException(INVALID_WRAPPER_ERROR + " : " + backend);
        }
        vulkan = (Vulkan10Wrapper) backend;
    }

    @Override
    public void render(RootNode root) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void endFrame() {
        // TODO Auto-generated method stub

    }

    @Override
    public void forceRenderMode(DrawMode mode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void render(RenderableNode<?> node) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void uploadTextures(Texture2D texture, BufferImage[] textureImages) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void uploadTextures(Image image, boolean generateMipmaps) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void prepareTexture(Texture2D texture, int unit) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void prepareTexture(Texture texture, int unit, Accessor accessor, ShaderVariable attribute,
            ShaderVariable texUniform, IntBuffer samplerUniformBuffer) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public BufferFactory getBufferFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Assets getAssets() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void renderMesh(GraphicsPipeline pipeline, Mesh mesh, float[][] matrices) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void renderPrimitive(GraphicsPipeline pipeline, GLTF glTF, Primitive primitive, float[][] matrices)
            throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawVertices(GraphicsPipeline pipeline, Accessor indices, int vertexCount,
            ArrayList<Attributes> attribs, ArrayList<Accessor> accessors, DrawMode mode) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean usePipeline(GraphicsPipeline pipeline) throws BackendException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void deletePipeline(Pipeline pipeline) {
        // TODO Auto-generated method stub

    }

}
