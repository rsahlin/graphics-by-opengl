package com.nucleus.renderer;

import java.util.ArrayList;

import com.nucleus.opengl.GLException;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer.NodeRenderer;
import com.nucleus.scene.GLTFNode;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.shader.ShaderProgram;

/**
 * Renders the glTF nodes
 *
 */
public class GLTFNodeRenderer extends NodeRenderer<GLTFNode, Mesh> {

    protected ArrayList<Mesh> nodeMeshes = new ArrayList<>();
    protected FrameSampler timeKeeper = FrameSampler.getInstance();

    @Override
    public void renderNode(NucleusRenderer renderer, GLTFNode node, Pass currentPass, float[][] matrices)
            throws GLException {

    }

    @Override
    public void renderMesh(NucleusRenderer renderer, ShaderProgram program, Mesh mesh, float[][] matrices)
            throws GLException {
        // TODO Auto-generated method stub

    }

}
