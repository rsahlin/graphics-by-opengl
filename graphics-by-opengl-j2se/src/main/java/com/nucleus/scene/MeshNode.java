package com.nucleus.scene;

import java.io.IOException;

import com.nucleus.common.Type;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureType;

/**
 * Node for a custom mesh, the mesh can either be created programmatically in runtime or by using a custom
 * implementation of MeshFactory when node is serialized.
 *
 */
public class MeshNode extends Node {

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    protected MeshNode() {
        super();
    }

    public MeshNode(RootNode root, Type<Node> type) {
        super(root, type);
    }

    /**
     * Creates a Mesh builder for this node.
     * Default behavior is to create a mesh builder using textured translate program and a fullscreen rectangle.
     * 
     * @param renderer
     * @param parent
     * @return
     * @throws IOException
     */
    @Override
    public Mesh.Builder<Mesh> createMeshBuilder(NucleusRenderer renderer, Node parent, int count,
            ShapeBuilder shapeBuilder) throws IOException {

        Mesh.Builder<Mesh> builder = new Mesh.Builder<>(renderer);
        Texture2D tex = TextureFactory.createTexture(TextureType.Untextured);
        builder.setTexture(tex);
        return initMeshBuilder(renderer, parent, count, shapeBuilder, builder);

    }

    @Override
    public Node createInstance(RootNode root) {
        MeshNode copy = new MeshNode(root, NodeTypes.meshnode);
        copy.set(this);
        return copy;
    }

}
