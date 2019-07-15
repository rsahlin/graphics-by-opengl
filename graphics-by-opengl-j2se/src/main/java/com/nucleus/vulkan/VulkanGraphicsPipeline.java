package com.nucleus.vulkan;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.shader.VariableIndexer;

public class VulkanGraphicsPipeline implements GraphicsPipeline {

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
    public void setUniformData(NamedShaderVariable variable, float[] data, int sourceOffset) {
        // TODO Auto-generated method stub

    }

    @Override
    public FloatBuffer getUniformData() {
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
    public VariableIndexer getLocationMapping() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void glVertexAttribPointer(ArrayList<Attributes> attribs, ArrayList<Accessor> accessors)
            throws BackendException {
        // TODO Auto-generated method stub

    }

    @Override
    public void compile(NucleusRenderer renderer, GraphicsShader shader) throws BackendException {
        // TODO Auto-generated method stub

    }

}
