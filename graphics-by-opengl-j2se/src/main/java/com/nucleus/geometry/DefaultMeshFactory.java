package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.assets.AssetManager;
import com.nucleus.geometry.Mesh.Mode;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.LineDrawerNode;
import com.nucleus.scene.Node;
import com.nucleus.shader.TranslateProgram;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureType;

public class DefaultMeshFactory implements MeshFactory {

    @Override
    public Mesh createMesh(NucleusRenderer renderer, Node parent) throws IOException, GLException {

        switch (Node.NodeTypes.valueOf(parent.getType())) {
            case linedrawernode:
                LineDrawerNode lineDrawer = (LineDrawerNode) parent;
                Mesh.Builder<Mesh> builder = new Mesh.Builder<>(renderer);
                int count = lineDrawer.getLineCount();
                switch (lineDrawer.getLineMode()) {
                    case LINES:
                        builder.setArrayMode(Mode.LINES, count * 2);
                        break;
                    case LINE_STRIP:
                        builder.setArrayMode(Mode.LINE_STRIP, count * 2);
                        break;
                    case RECTANGLE:
                        // Rectangle shares vertices, 4 vertices per rectangle
                        builder.setElementMode(Mode.LINES, count, count * 2);
                        break;
                    default:
                        throw new IllegalArgumentException("Not implemented for mode " + lineDrawer.getLineMode());
                }
                Material m = new Material();
                TranslateProgram program = (TranslateProgram) AssetManager.getInstance()
                        .getProgram(renderer, new TranslateProgram(Shading.flat));
                m.setProgram(program);
                Texture2D tex = TextureFactory.createTexture(TextureType.Untextured);
                builder.setMaterial(m);
                builder.setTexture(tex);
                builder.setShapeBuilder(((LineDrawerNode) parent).getShapeBuilder());
                // RectangleShapeBuilder.Configuration config = new RectangleShapeBuilder.Configuration(0.5f, 0.5f, 0f,
                // 1,
                // 0);
                // builder.setShapeBuilder(new RectangleShapeBuilder(config));
                Mesh mesh = builder.create();
                return mesh;
            case switchnode:
            case layernode:
                // No mesh for switch node, or layernode
                return null;
            default:
                throw new IllegalArgumentException("Not implemented for " + parent.getType());
        }
    }

}
