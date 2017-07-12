package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.io.ExternalReference;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.shader.ShaderProgram;

public class DefaultMeshFactory implements MeshFactory {

    @Override
    public Mesh createMesh(NucleusRenderer renderer, Node source) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public Mesh createMesh(NucleusRenderer renderer, ShaderProgram program, Material material,
            ExternalReference textureRef, int vertexCount, int indiceCount) throws IOException {
        throw new IllegalArgumentException("Not implemented");
    }


}
