package com.nucleus.geometry;

import com.nucleus.io.ResourcesData;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;

public class DefaultMeshFactory implements MeshFactory {

    @Override
    public Mesh createMesh(NucleusRenderer renderer, Node source, ResourcesData resources) {
        throw new IllegalArgumentException("Not implemented");
    }

}
