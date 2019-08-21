package com.nucleus.renderer;

import java.util.ArrayList;

import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.Mesh;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.scene.RenderableNode;
import com.nucleus.shader.GraphicsShader;

public class DefaultNodeRenderer implements NodeRenderer<RenderableNode<Mesh>> {

    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();
    transient protected ArrayList<Mesh> nodeMeshes = new ArrayList<>();

    @Override
    public boolean renderNode(NucleusRenderer renderer, RenderableNode<Mesh> node, Pass currentPass, float[][] matrices)
            throws BackendException {
        renderMeshes(renderer, node, currentPass, matrices);
        return true;
    }

    public void renderMesh(NucleusRenderer renderer, GraphicsPipeline<?> pipeline, Mesh mesh, float[][] matrices)
            throws BackendException {
        Consumer updater = mesh.getAttributeConsumer();
        if (updater != null) {
            updater.updateAttributeData(renderer);
        }
        if (mesh.getDrawCount() == 0) {
            return;
        }
        renderer.renderMesh(pipeline, mesh, matrices);
    }

    public boolean renderMeshes(NucleusRenderer renderer, RenderableNode<Mesh> node, Pass currentPass,
            float[][] matrices) throws BackendException {
        nodeMeshes.clear();
        node.getMeshes(nodeMeshes);
        if (nodeMeshes.size() > 0) {
            GraphicsPipeline<?> pipeline = getPipeline(renderer, node, currentPass);
            renderer.usePipeline(pipeline);
            GraphicsShader program = node.getProgram();
            program.setUniformMatrices(matrices);
            program.updateUniformData();
            program.uploadUniforms();
            for (Mesh mesh : nodeMeshes) {
                renderMesh(renderer, pipeline, mesh, matrices);
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
    protected GraphicsPipeline<?> getPipeline(NucleusRenderer renderer, RenderableNode<Mesh> node, Pass pass) {
        GraphicsPipeline<?> pipeline = node.getProgram().getPipeline();
        if (pipeline == null) {
            throw new IllegalArgumentException("No pipeline for node " + node.getId());
        }
        // TODO Not implemented yet
        return pipeline;
    }

}
