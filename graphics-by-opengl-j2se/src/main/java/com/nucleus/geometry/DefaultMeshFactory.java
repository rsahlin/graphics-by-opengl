package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.LineDrawerNode;
import com.nucleus.scene.MeshNode;
import com.nucleus.scene.RenderableNode;

/**
 * TODO cleanup and use Mesh.Builder as much as possible - should this class be removed in favor of Builder?
 *
 */
public class DefaultMeshFactory implements MeshFactory<Mesh> {

    private MeshFactory.MeshCreator<Mesh> customMeshCreator;

    @Override
    public Mesh createMesh(NucleusRenderer renderer, RenderableNode<Mesh> parent) throws IOException, GLException {

        if (parent instanceof LineDrawerNode) {
            LineDrawerNode lineParent = (LineDrawerNode) parent;
            MeshBuilder<Mesh> builder = parent.createMeshBuilder(renderer, parent, lineParent.getLineCount(),
                    lineParent.getShapeBuilder());
            Mesh mesh = builder.create();
            if (mesh != null) {
                parent.addMesh(mesh);
            }
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
    public void setMeshCreator(MeshCreator<Mesh> creator) {
        this.customMeshCreator = creator;
    }

}
