package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.LineDrawerNode;
import com.nucleus.scene.MeshNode;
import com.nucleus.scene.Node;

/**
 * TODO cleanup and use Mesh.Builder as much as possible - should this class be removed in favor of Builder?
 *
 */
public class DefaultMeshFactory implements MeshFactory {

    private MeshFactory.MeshCreator customMeshCreator;

    @Override
    public Mesh createMesh(NucleusRenderer renderer, Node parent) throws IOException, GLException {

        if (parent instanceof LineDrawerNode) {
            LineDrawerNode lineParent = (LineDrawerNode) parent;
            Mesh.Builder<Mesh> builder = parent.createMeshBuilder(renderer, parent, lineParent.getLineCount(),
                    lineParent.getShapeBuilder());
            Mesh mesh = builder.create();
            return mesh;

        }
        if (customMeshCreator != null) {
            return customMeshCreator.createCustomMesh(renderer, parent);
        }
        if (parent instanceof MeshNode) {
            return parent.createMeshBuilder(renderer, parent, 1, null).create();
        }
        return null;
    }

    @Override
    public void setMeshCreator(MeshCreator creator) {
        this.customMeshCreator = creator;
    }

}
