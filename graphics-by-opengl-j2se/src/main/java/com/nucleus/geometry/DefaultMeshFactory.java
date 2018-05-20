package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.assets.AssetManager;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.LineDrawerNode;
import com.nucleus.scene.MeshNode;
import com.nucleus.scene.Node;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.TranslateProgram;
import com.nucleus.texturing.Texture2D.Shading;

/**
 * TODO cleanup and use Mesh.Builder as much as possible - should this class be removed in favor of Builder?
 *
 */
public class DefaultMeshFactory implements MeshFactory {

    private MeshFactory.MeshCreator customMeshCreator;

    @Override
    public Mesh createMesh(NucleusRenderer renderer, Node parent) throws IOException, GLException {

        if (parent instanceof LineDrawerNode) {
            if (parent.getProgram() == null) {
                ShaderProgram program = AssetManager.getInstance()
                        .getProgram(renderer.getGLES(), new TranslateProgram(Shading.flat));
                parent.setProgram(program);
            }
            Mesh.Builder<Mesh> builder = LineDrawerNode.createMeshBuilder(renderer, (LineDrawerNode) parent);
            Mesh mesh = builder.create();
            if (parent.getProgram() == null) {
                parent.setProgram(builder.program);
            }
            return mesh;

        }
        if (customMeshCreator != null) {
            return customMeshCreator.createCustomMesh(renderer, parent);
        }
        if (parent instanceof MeshNode) {
            return MeshNode.createMeshBuilder(renderer, (MeshNode) parent).create();
        }
        return null;
    }

    @Override
    public void setMeshCreator(MeshCreator creator) {
        this.customMeshCreator = creator;
    }

}
