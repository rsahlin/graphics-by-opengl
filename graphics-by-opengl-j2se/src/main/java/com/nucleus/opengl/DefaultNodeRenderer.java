package com.nucleus.opengl;

import java.util.ArrayList;

import com.nucleus.common.Environment;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.Backend.DrawMode;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.RenderableNode;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Texture2D;

public class DefaultNodeRenderer implements NodeRenderer<RenderableNode<Mesh>> {

    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();
    transient protected ArrayList<Mesh> nodeMeshes = new ArrayList<>();
    transient protected DrawMode forceMode = null;

    @Override
    public void forceRenderMode(DrawMode mode) {
        this.forceMode = mode;
    }

    @Override
    public boolean renderNode(NucleusRenderer renderer, RenderableNode<Mesh> node, Pass currentPass, float[][] matrices)
            throws GLException {
        renderMeshes(renderer, node, currentPass, matrices);
        return true;
    }

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
        int mode = gles.getDrawMode(mesh.getMode());
        ElementBuffer indices = mesh.getElementBuffer();
        if (indices == null) {
            gles.glDrawArrays(mode, mesh.getOffset(), mesh.getDrawCount());
            GLUtils.handleError(gles, "glDrawArrays ");
            timeKeeper.addDrawArrays(mesh.getDrawCount());
        } else {
            if (indices.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
                gles.glDrawElements(mode, mesh.getDrawCount(), indices.getType().type,
                        mesh.getOffset());
                GLUtils.handleError(gles, "glDrawElements with ElementBuffer " + mesh.getMode() + ", "
                        + mesh.getDrawCount() + ", " + indices.getType() + ", " + mesh.getOffset());
            } else {
                gles.glDrawElements(mode, mesh.getDrawCount(), indices.getType().type,
                        indices.getBuffer().position(mesh.getOffset()));
                GLUtils.handleError(gles, "glDrawElements no ElementBuffer ");
            }
            AttributeBuffer vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES_STATIC);
            if (vertices == null) {
                vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES);
            }
            timeKeeper.addDrawElements(vertices.getVerticeCount(), mesh.getDrawCount());
        }
        gles.disableAttribPointers();
    }

    public boolean renderMeshes(NucleusRenderer renderer, RenderableNode<Mesh> node, Pass currentPass,
            float[][] matrices) throws GLException {
        nodeMeshes.clear();
        node.getMeshes(nodeMeshes);
        if (nodeMeshes.size() > 0) {
            GLES20Wrapper gles = renderer.getGLES();
            ShaderProgram program = getProgram(gles, node, currentPass);
            gles.glUseProgram(program.getProgram());
            GLUtils.handleError(gles, "glUseProgram " + program.getProgram());
            // TODO - is this the best place for this check - remember, this should only be done in debug cases.
            if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
                program.validateProgram(gles);
            }
            for (Mesh mesh : nodeMeshes) {
                renderMesh(renderer, program, mesh, matrices);
            }
            return true;
        }
        return false;
    }

    /**
     * 
     * @param node The node being rendered
     * @param pass The currently defined pass
     * @return
     */
    protected ShaderProgram getProgram(GLES20Wrapper gles, RenderableNode<Mesh> node, Pass pass) {
        ShaderProgram program = node.getProgram();
        if (program == null) {
            throw new IllegalArgumentException("No program for node " + node.getId());
        }
        return program.getProgram(gles, pass, program.getShading());
    }

}
