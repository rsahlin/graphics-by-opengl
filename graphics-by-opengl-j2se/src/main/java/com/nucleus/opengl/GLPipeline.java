package com.nucleus.opengl;

import java.util.ArrayList;

import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.common.Environment;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.shader.GLShaderProgram;
import com.nucleus.opengl.shader.GLTFShaderProgram;
import com.nucleus.opengl.shader.ShaderVariable;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderState.Cullface;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Material.AlphaMode;
import com.nucleus.scene.gltf.PBRMetallicRoughness;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.texturing.Texture2D;

/**
 * A pipeline holds the data that is needed for the processing stages.
 * Some of this data is immutable, such as shader and blend function
 *
 */
public class GLPipeline extends GraphicsPipeline {

    protected NucleusRenderer renderer;
    protected GLShaderProgram shader;
    protected Material material;
    protected RenderState renderState;

    public GLPipeline(NucleusRenderer renderer, GLShaderProgram shader, Material material) {
        this.renderer = renderer;
        this.shader = shader;
        this.material = material;
        renderState = renderer.getRenderState();
    }

    @Override
    public void enable(NucleusRenderer renderer) throws BackendException {
        GLES20Wrapper gles = renderer.getGLES();
        int program = shader.getProgram();
        gles.glUseProgram(program);
        GLUtils.handleError(gles, "glUseProgram " + program);
        // TODO - is this the best place for this check - remember, this should only be done in debug cases.
        if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
            shader.validateProgram(gles);
        }

    }

    @Override
    public void update(NucleusRenderer renderer, Mesh mesh, float[][] matrices) throws BackendException {
        GLES20Wrapper gles = renderer.getGLES();
        shader.setUniformMatrices(matrices);
        shader.updateUniformData(shader.getUniformData());

        shader.updateAttributes(gles, mesh);
        shader.uploadUniforms(gles);
        shader.prepareTexture(renderer, mesh.getTexture(Texture2D.TEXTURE_0));
        material.setBlendModeSeparate(gles);

    }

    @Override
    public void update(NucleusRenderer renderer, GLTF gltf, Primitive primitive, float[][] matrices)
            throws BackendException {
        ((GLTFShaderProgram) shader).updateEnvironmentUniforms(renderer, gltf.getDefaultScene());
        // Can be optimized to update uniforms under the following conditions:
        // The program has changed OR the matrices have changed, ie another parent node.
        GLES20Wrapper gles = renderer.getGLES();
        shader.setUniformMatrices(matrices);
        shader.updateUniformData(shader.getUniformData());
        shader.uploadUniforms(gles);
        com.nucleus.scene.gltf.Material material = primitive.getMaterial();
        if (material != null) {
            PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
            // Check for doublesided.
            if (material.isDoubleSided() && renderState.getCullFace() != Cullface.NONE) {
                gles.glDisable(GLES20.GL_CULL_FACE);
            }
            ((GLTFShaderProgram) shader).prepareTextures(renderer, gltf, primitive, material);
            if (material.getAlphaMode() == AlphaMode.OPAQUE) {
                gles.glDisable(GLES20.GL_BLEND);
            } else {
                gles.glEnable(GLES20.GL_BLEND);
            }
        }
        ((GLTFShaderProgram) shader).updatePBRUniforms(gles, primitive);
    }

    @Override
    public void glVertexAttribPointer(ArrayList<Attributes> attribs, ArrayList<Accessor> accessors)
            throws BackendException {
        GLES20Wrapper gles = renderer.getGLES();
        for (int i = 0; i < attribs.size(); i++) {
            Accessor accessor = accessors.get(i);
            ShaderVariable v = shader.getAttributeByName(attribs.get(i).name());
            if (v != null) {
                gles.glVertexAttribPointer(shader, accessor, v);
            } else {
                // TODO - when fully implemented this should not happen.
            }
        }
        GLUtils.handleError(gles, "glVertexAttribPointer");
    }

    @Override
    public int[] getAttributeSizes() {
        return shader.getAttributeSizes();
    }

    @Override
    public ShaderVariable getUniformByName(String uniform) {
        return shader.getUniformByName(uniform);
    }

    @Override
    public ShaderVariable getAttributeByName(String attribute) {
        return shader.getAttributeByName(attribute);
    }

    @Override
    public int getAttributesPerVertex(BufferIndex buffer) {
        return shader.getAttributesPerVertex(buffer);
    }

}
