package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.common.Type;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.NodeRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderProgram;

public class RenderToTargetNode extends NucleusMeshNode<Mesh> {

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    @Deprecated
    protected RenderToTargetNode() {
    }

    protected RenderToTargetNode(RootNode root, Type<Node> type) {
        super(root, type);
    }

    private RenderToTargetNode(RootNode root) {
        super(root, NodeTypes.rendertotarget);
    }

    @Override
    public RenderToTargetNode createInstance(RootNode root) {
        RenderToTargetNode copy = new RenderToTargetNode(root);
        copy.set(this);
        return copy;
    }

    @Override
    public boolean renderNode(NucleusRenderer renderer, Pass currentPass, float[][] matrices) throws GLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ArrayList<Mesh> getMeshes(ArrayList<Mesh> list) {
        list.addAll(meshes);
        return list;
    }

    @Override
    public void create() {
        // TODO Auto-generated method stub

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

}
