package com.nucleus.renderer;

import java.util.ArrayList;

import com.nucleus.common.Environment;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.scene.RenderableNode;
import com.nucleus.shader.ShaderProgram;

/**
 * The default noderenderer - renders the meshes in a node.
 * 
 *
 * @param <T>
 */
public class NucleusNodeRenderer<T extends RenderableNode<Mesh>>
        extends com.nucleus.renderer.NucleusRenderer.NodeRenderer<T> {

    protected ArrayList<Mesh> nodeMeshes = new ArrayList<>();
    protected FrameSampler timeKeeper = FrameSampler.getInstance();

    public NucleusNodeRenderer() {
    }

    @Override
    public void renderNode(NucleusRenderer renderer, T node, Pass currentPass, float[][] matrices)
            throws GLException {
        GLES20Wrapper gles = renderer.getGLES();
        nodeMeshes.clear();
        node.getMeshes(nodeMeshes);
        if (nodeMeshes.size() > 0) {
            ShaderProgram program = getProgram(gles, node, currentPass);
            gles.glUseProgram(program.getProgram());
            GLUtils.handleError(gles, "glUseProgram " + program.getProgram());
            // TODO - is this the best place for this check - remember, this should only be done in debug cases.
            if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
                program.validateProgram(gles);
            }
            renderMeshes(renderer, program, nodeMeshes, matrices);
        }
    }

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
        program.updateUniforms(gles, matrices, mesh);
        program.prepareTextures(gles, mesh);

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

    /**
     * 
     * @param node The node being rendered
     * @param pass The currently defined pass
     * @return
     */
    protected ShaderProgram getProgram(GLES20Wrapper gles, RenderableNode<?> node, Pass pass) {
        ShaderProgram program = node.getProgram();
        if (program == null) {
            throw new IllegalArgumentException("No program for node " + node.getId());
        }
        return program.getProgram(gles, pass, program.getShading());
    }

    /**
     * Renders the meshes in this node.
     * 
     * @param renderer
     * @param program
     * @param meshes
     * @param matrices
     * @throws GLException
     */
    protected void renderMeshes(NucleusRenderer renderer, ShaderProgram program, ArrayList<Mesh> meshes,
            float[][] matrices) throws GLException {
        for (Mesh mesh : meshes) {
            renderMesh(renderer, program, mesh, matrices);
        }
    }

}
