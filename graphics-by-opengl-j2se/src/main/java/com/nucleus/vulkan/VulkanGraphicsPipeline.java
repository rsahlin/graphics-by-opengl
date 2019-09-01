package com.nucleus.vulkan;

import java.io.IOException;
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
import com.nucleus.vulkan.structs.ShaderModuleCreateInfo;

public class VulkanGraphicsPipeline implements GraphicsPipeline<ShaderBinary> {

    /**
     * Shader type sourcename
     */
    public static final String FRAGMENT_TYPE = "_fs";
    public static final String VERTEX_TYPE = "_vs";
    public static final String GEOMETRY_TYPE = "_gs";
    public static final String COMPUTE_TYPE = "_cs";

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
        String sourceNameVersion = ShaderBinary.getSourceNameVersion(version);
        switch (type) {
            case VERTEX:
                return new ShaderModuleCreateInfo(ShaderBinary.PROGRAM_DIRECTORY + sourceNameVersion,
                        function.getShaderSourceName(type), VERTEX_TYPE, type);
            case FRAGMENT:
                return new ShaderModuleCreateInfo(ShaderBinary.PROGRAM_DIRECTORY + sourceNameVersion,
                        function.getShaderSourceName(type), FRAGMENT_TYPE, type);
            case COMPUTE:
                return new ShaderModuleCreateInfo(ShaderBinary.PROGRAM_DIRECTORY + sourceNameVersion,
                        function.getShaderSourceName(type), COMPUTE_TYPE, type);
            case GEOMETRY:
                return new ShaderModuleCreateInfo(ShaderBinary.PROGRAM_DIRECTORY + sourceNameVersion,
                        function.getShaderSourceName(type), GEOMETRY_TYPE, type);

            default:
                throw new IllegalArgumentException("Not implemented for type: " + type);
        }
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

        ShaderBinary vertex = getShaderSource(renderer.getBackend().getVersion(), shader.getFunction(),
                ShaderType.VERTEX);
        try {
            vertex.loadShader(renderer.getBackend(), shader.getFunction());
        } catch (IOException e) {
            throw new BackendException(e);
        }
    }

    @Override
    public void createProgram(NucleusRenderer renderer, ShaderBinary[] sources, GraphicsShader shader)
            throws BackendException {
        // TODO Auto-generated method stub
    }

}
