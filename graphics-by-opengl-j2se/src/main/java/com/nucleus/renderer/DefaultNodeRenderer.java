package com.nucleus.renderer;

import java.util.ArrayList;

import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.shader.ShaderProgram;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.scene.RenderableNode;

public class DefaultNodeRenderer implements NodeRenderer<RenderableNode<Mesh>> {

    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();
    transient protected ArrayList<Mesh> nodeMeshes = new ArrayList<>();

    @Override
    public boolean renderNode(NucleusRenderer renderer, RenderableNode<Mesh> node, Pass currentPass, float[][] matrices)
            throws RenderBackendException {
        renderMeshes(renderer, node, currentPass, matrices);
        return true;
    }

    public void renderMesh(NucleusRenderer renderer, ShaderProgram program, Mesh mesh, float[][] matrices)
            throws RenderBackendException {
        Consumer updater = mesh.getAttributeConsumer();
        if (updater != null) {
            updater.updateAttributeData(renderer);
        }
        if (mesh.getDrawCount() == 0) {
            return;
        }
        renderer.renderMesh(program, mesh, matrices);
    }

    public boolean renderMeshes(NucleusRenderer renderer, RenderableNode<Mesh> node, Pass currentPass,
            float[][] matrices) throws RenderBackendException {
        nodeMeshes.clear();
        node.getMeshes(nodeMeshes);
        if (nodeMeshes.size() > 0) {
            ShaderProgram program = getProgram(renderer, node, currentPass);
            renderer.useProgram(program);
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
    protected ShaderProgram getProgram(NucleusRenderer renderer, RenderableNode<Mesh> node, Pass pass) {
        ShaderProgram program = node.getProgram();
        if (program == null) {
            throw new IllegalArgumentException("No program for node " + node.getId());
        }
        return program.getProgram(renderer, pass, program.getShading());
    }

}
