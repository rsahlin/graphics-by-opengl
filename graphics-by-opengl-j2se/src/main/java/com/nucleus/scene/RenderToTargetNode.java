package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.common.Type;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.NodeRenderer;
import com.nucleus.renderer.Pass;

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
    public NodeRenderer<?> getNodeRenderer() {
        // TODO Auto-generated method stub
        return null;
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

}
