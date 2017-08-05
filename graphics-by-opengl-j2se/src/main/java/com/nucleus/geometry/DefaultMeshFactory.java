package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;

public class DefaultMeshFactory implements MeshFactory {

    @Override
    public Mesh createMesh(NucleusRenderer renderer, Node source) throws IOException {
        throw new IllegalArgumentException("Not implemented");
    }

}
