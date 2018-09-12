package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.common.Type;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.MeshRenderer;
import com.nucleus.renderer.NucleusMeshRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;

public class RenderToTargetNode extends AbstractMeshNode<Mesh> {

    transient protected static MeshRenderer<Mesh> meshRenderer;
    
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
    protected void createMeshRenderer() {
        if (meshRenderer == null) {
            meshRenderer = new NucleusMeshRenderer();
        }
    }
    
    @Override
    public MeshRenderer<Mesh> getMeshRenderer() {
        return meshRenderer;
    }
    

}
