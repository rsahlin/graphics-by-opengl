package com.nucleus.geometry;

import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;

public class DefaultMeshFactory implements MeshFactory {

    @Override
    public Mesh createMesh(NucleusRenderer renderer, Node source, Node.MeshType type) {
        throw new IllegalArgumentException("Not implemented");
    }

}
