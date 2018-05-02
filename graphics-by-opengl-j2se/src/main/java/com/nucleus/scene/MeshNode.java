package com.nucleus.scene;

import java.io.IOException;

import com.nucleus.assets.AssetManager;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.Mode;
import com.nucleus.geometry.RectangleShapeBuilder;
import com.nucleus.geometry.RectangleShapeBuilder.RectangleConfiguration;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.TranslateProgram;
import com.nucleus.texturing.Texture2D.Shading;

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
    public static Mesh.Builder<Mesh> createMeshBuilder(NucleusRenderer renderer, MeshNode parent) throws IOException {
        LayerNode layer = parent.getRootNode().getLayerNode(null);
        ViewFrustum view = layer.getViewFrustum();
        ShaderProgram program = AssetManager.getInstance()
                .getProgram(renderer.getGLES(), new TranslateProgram(Shading.textured));
        Mesh.Builder<Mesh> builder = Mesh.createBuilder(renderer, 4,
                parent.getMaterial() != null ? parent.getMaterial() : new Material(),
                program, AssetManager.getInstance().getTexture(renderer, parent.getTextureRef()),
                new RectangleShapeBuilder(
                        new RectangleConfiguration(view.getWidth(), view.getHeight(), 0f, 1, 0)),
                Mode.TRIANGLE_FAN);
        return builder;

    }

    @Override
    public Node createInstance(RootNode root) {
        MeshNode copy = new MeshNode(root, NodeTypes.meshnode);
        copy.set(this);
        return copy;
    }

}
