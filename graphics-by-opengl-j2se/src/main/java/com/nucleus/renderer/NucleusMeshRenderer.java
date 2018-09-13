package com.nucleus.renderer;

import java.util.ArrayList;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.scene.RenderableNode;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Texture2D;

/**
 * Implementation that can render the default Nucleus Mesh
 *
 */
public class NucleusMeshRenderer implements MeshRenderer<Mesh>{

    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();
    transient protected ArrayList<Mesh> nodeMeshes = new ArrayList<>();
	
    @Override
    public void renderMesh(NucleusRenderer renderer, ShaderProgram program, Mesh mesh, float[][] matrices)
            throws GLException {
        GLES20Wrapper gles = renderer.getGLES();
        Consumer updater = mesh.getAttributeConsumer();
        if (updater != null) {
            updater.updateAttributeData(renderer);
        }
        if (mesh.getDrawCount() == 0) {
            return;
        }
        Material material = mesh.getMaterial();

        program.updateAttributes(gles, mesh);
        program.updateUniforms(gles, matrices);
        program.prepareTexture(gles, mesh.getTexture(Texture2D.TEXTURE_0));

        material.setBlendModeSeparate(gles);

        ElementBuffer indices = mesh.getElementBuffer();

        if (indices == null) {
            gles.glDrawArrays(mesh.getMode().mode, mesh.getOffset(), mesh.getDrawCount());
            GLUtils.handleError(gles, "glDrawArrays ");
            timeKeeper.addDrawArrays(mesh.getDrawCount());
        } else {
            if (indices.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
                gles.glDrawElements(mesh.getMode().mode, mesh.getDrawCount(), indices.getType().type,
                        mesh.getOffset());
                GLUtils.handleError(gles, "glDrawElements with ElementBuffer ");
            } else {
                gles.glDrawElements(mesh.getMode().mode, mesh.getDrawCount(), indices.getType().type,
                        indices.getBuffer().position(mesh.getOffset()));
                GLUtils.handleError(gles, "glDrawElements no ElementBuffer ");
            }
            AttributeBuffer vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES_STATIC);
            if (vertices == null) {
                vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES);
            }
            timeKeeper.addDrawElements(vertices.getVerticeCount(), mesh.getDrawCount());
        }

    }

	@Override
	public boolean renderMeshes(NucleusRenderer renderer, ShaderProgram program, RenderableNode<Mesh> node,
			float[][] matrices) throws GLException {
		
        GLES20Wrapper gles = renderer.getGLES();
        nodeMeshes.clear();
        node.getMeshes(nodeMeshes);
        if (nodeMeshes.size() > 0) {
            for (Mesh mesh : nodeMeshes) {
                renderMesh(renderer, program, mesh, matrices);
            }
            return true;
        }
		return false;
	}

}
