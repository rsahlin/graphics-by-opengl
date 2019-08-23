package com.nucleus.vulkan;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.shader.BlockBuffer;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.shader.Shader.Categorizer;
import com.nucleus.shader.Shader.ShaderType;
import com.nucleus.shader.ShaderBinary;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.shader.VariableIndexer;

public class VulkanGraphicsPipeline implements GraphicsPipeline<ShaderBinary> {

    private Vulkan10Wrapper vulkan;

    public VulkanGraphicsPipeline(Vulkan10Wrapper vulkan) {
        if (vulkan == null) {
            throw new IllegalArgumentException("Vulkan wrapper is null");
        }
        this.vulkan = vulkan;
    }

    @Override
    public int[] getAttributeSizes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NamedShaderVariable getUniformByName(String uniform) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NamedShaderVariable getAttributeByName(String attribute) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void enable(NucleusRenderer renderer) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void update(NucleusRenderer renderer, Mesh mesh, float[][] matrices) throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void update(NucleusRenderer renderer, GLTF gltf, Primitive primitive, float[][] matrices)
            throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy(NucleusRenderer renderer) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getFilenameSuffix(ShaderType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void glVertexAttribPointer(ArrayList<Attributes> attribs, ArrayList<Accessor> accessors)
            throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void uploadVariable(FloatBuffer data, ShaderVariable variable) {
        // TODO Auto-generated method stub

    }

    @Override
    public void uploadUniforms(FloatBuffer uniformData, ShaderVariable[] activeUniforms) {
        // TODO Auto-generated method stub

    }

    @Override
    public void uploadAttributes(FloatBuffer attributeData, ShaderVariable[] activeAttributes) {
        // TODO Auto-generated method stub

    }

    @Override
    public ShaderBinary getShaderSource(Renderers version, Categorizer function, ShaderType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getVariableSize(VariableType type) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ShaderVariable[] getActiveVariables(VariableType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockBuffer[] getUniformBlocks() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VariableIndexer getLocationMapping() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void compile(NucleusRenderer renderer, GraphicsShader shader) throws BackendException {

    }

    @Override
    public void createProgram(NucleusRenderer renderer, ShaderBinary[] sources, GraphicsShader shader)
            throws BackendException {
        // TODO Auto-generated method stub

    }

}
