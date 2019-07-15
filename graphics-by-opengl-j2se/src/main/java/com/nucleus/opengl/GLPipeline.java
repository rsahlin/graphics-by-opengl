package com.nucleus.opengl;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.assets.Assets;
import com.nucleus.common.Environment;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.shader.GLShaderProgram;
import com.nucleus.opengl.shader.GLTFShaderProgram;
import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Material.AlphaMode;
import com.nucleus.scene.gltf.PBRMetallicRoughness;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.shader.VariableIndexer;
import com.nucleus.texturing.Texture2D;

/**
 * A pipeline holds the data that is needed for the processing stages.
 * Some of this data is immutable, such as shader and blend function
 *
 */
public class GLPipeline implements GraphicsPipeline {

    protected GLShaderProgram shader;
    protected GLES20Wrapper gles;

    /**
     * Internal constructor - do not call directly, use
     * {@link Assets#getGraphicsPipeline(NucleusRenderer, GLShaderProgram)}
     * 
     * @param gles
     */
    public GLPipeline(GLES20Wrapper gles) {
        if (gles == null) {
            throw new IllegalArgumentException("GLES wrapper is null");
        }
        this.gles = gles;
    }

    @Override
    public void enable(NucleusRenderer renderer) throws BackendException {
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
        shader.setUniformMatrices(matrices);
        shader.updateUniformData(shader.getUniformData());

        shader.updateAttributes(gles, mesh);
        shader.uploadUniforms(gles);
        shader.prepareTexture(renderer, mesh.getTexture(Texture2D.TEXTURE_0));
        mesh.getMaterial().setBlendModeSeparate(gles);

    }

    @Override
    public void update(NucleusRenderer renderer, GLTF gltf, Primitive primitive, float[][] matrices)
            throws BackendException {
        ((GLTFShaderProgram) shader).updateEnvironmentUniforms(renderer, gltf.getDefaultScene());
        // Can be optimized to update uniforms under the following conditions:
        // The program has changed OR the matrices have changed, ie another parent node.
        shader.setUniformMatrices(matrices);
        shader.updateUniformData(shader.getUniformData());
        shader.uploadUniforms(gles);
        com.nucleus.scene.gltf.Material material = primitive.getMaterial();
        if (material != null) {
            PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
            // Check for doublesided.
            if (material.isDoubleSided()) {
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
        for (int i = 0; i < attribs.size(); i++) {
            Accessor accessor = accessors.get(i);
            NamedShaderVariable v = shader.getAttributeByName(attribs.get(i).name());
            if (v != null) {
                gles.glVertexAttribPointer(accessor, v);
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
    public NamedShaderVariable getUniformByName(String uniform) {
        return shader.getUniformByName(uniform);
    }

    @Override
    public NamedShaderVariable getAttributeByName(String attribute) {
        return shader.getAttributeByName(attribute);
    }

    @Override
    public void destroy(NucleusRenderer renderer) {
        if (shader != null) {
            gles.glDeleteProgram(shader.getProgram());
            shader = null;
        }
    }

    @Override
    public FloatBuffer getUniformData() {
        return shader.getUniformData();
    }

    @Override
    public void setUniformData(NamedShaderVariable variable, float[] data, int sourceOffset) {
        shader.setUniformData(variable, data, sourceOffset);
    }

    @Override
    public VariableIndexer getLocationMapping() {
        return shader.getIndexer();
    }

    @Override
    public void compile(NucleusRenderer renderer, GraphicsShader shader) throws BackendException {
        this.shader = ((GLShaderProgram) shader).createProgram(renderer);
    }

}
