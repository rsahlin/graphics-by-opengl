package com.nucleus.geometry;

import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;

public class DefaultMeshFactory implements MeshFactory {

    @Override
    public Mesh createMesh(NucleusRenderer renderer, Node source, RootNode scene) {
        throw new IllegalArgumentException("Not implemented");
    }

}
