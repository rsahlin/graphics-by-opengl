package com.nucleus.vulkan;

import java.nio.Buffer;
import java.util.ArrayList;

import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.shader.GLTFShaderProgram;
import com.nucleus.opengl.shader.ShaderProgram;
import com.nucleus.renderer.Backend.DrawMode;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RenderBackendException;
import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.SurfaceConfiguration;
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

public class VulkanBaseRenderer implements NucleusRenderer {

    @Override
    public void contextCreated(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void resizeWindow(int x, int y, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(SurfaceConfiguration surfaceConfig, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public SurfaceConfiguration getSurfaceConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float beginFrame() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void render(RootNode root) throws RenderBackendException {
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
    public void render(RenderableNode<?> node) throws RenderBackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void renderMesh(ShaderProgram program, Mesh mesh, float[][] matrices) throws RenderBackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void renderPrimitive(GLTFShaderProgram program, GLTF glTF, Primitive primitive, float[][] matrices)
            throws RenderBackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawVertices(ShaderProgram program, Accessor indices, int vertexCount, ArrayList<Attributes> attribs,
            ArrayList<Accessor> accessors, DrawMode mode) throws RenderBackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isInitialized() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addContextListener(RenderContextListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addFrameListener(FrameListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public GLES20Wrapper getGLES() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteBuffers(int count, int[] names, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindBuffer(int target, int buffer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bufferData(int target, int size, Buffer data, int usage) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProjection(float[] matrix, int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public RenderState getRenderState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int[] createTextureName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void uploadTextures(Texture2D texture, BufferImage[] textureImages) throws RenderBackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void uploadTextures(Image image, boolean generateMipmaps) throws RenderBackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void prepareTexture(Texture2D texture, int unit) throws RenderBackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void prepareTexture(Texture texture, int unit) throws RenderBackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean useProgram(ShaderProgram program) throws RenderBackendException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void createTexture(Texture2D texture, int target) throws RenderBackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteTextures(int[] names) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePrograms(int[] names) {
        // TODO Auto-generated method stub

    }

}
