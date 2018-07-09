package com.nucleus.scene;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.Mode;
import com.nucleus.geometry.shape.RectangleShapeBuilder;
import com.nucleus.geometry.shape.RectangleShapeBuilder.RectangleConfiguration;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.geometry.shape.ShapeBuilderFactory;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.NodeRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureType;
import com.nucleus.vecmath.Shape;

/**
 * Node for a custom mesh, the mesh can either be created programmatically in runtime or by using a custom
 * implementation of MeshFactory when node is serialized.
 *
 */
public class MeshNode extends Node {

    /**
     * If defined this is the shape of the mesh - if not specified a fullscreen 2D rect will be created.
     */
    @SerializedName(Shape.SHAPE)
    protected Shape shape;

    transient static NodeRenderer<MeshNode> nodeRenderer = new com.nucleus.renderer.NucleusNodeRenderer<MeshNode>();

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    protected MeshNode() {
        super();
    }

    private MeshNode(RootNode root, Type<Node> type) {
        super(root, type);
    }

    /**
     * Creates a Mesh builder for this node.
     * if shapebuilder is null then Mode is set to TRIANGLE_FAN with 4 vertices and a rectangle shapebuilder is created.
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
        Texture2D tex = null;
        if (parent.getTextureRef() == null) {
            tex = TextureFactory.createTexture(TextureType.Untextured);

        } else {
            tex = AssetManager.getInstance().getTexture(renderer, parent.getTextureRef());
        }
        builder.setTexture(tex);
        if (shapeBuilder == null) {
            LayerNode layer = parent.getRootNode().getLayerNode(null);
            ViewFrustum view = layer.getViewFrustum();
            builder.setArrayMode(Mode.TRIANGLE_FAN, 4, 0);
            if (shape == null) {
                shapeBuilder = new RectangleShapeBuilder(
                        new RectangleConfiguration(view.getWidth(), view.getHeight(), 0f, 1, 0));
            } else {
                shapeBuilder = ShapeBuilderFactory.createBuilder(shape, count, 0);
            }
        }
        // If program is not present in parent then the meshbuilder is called to create program.
        return initMeshBuilder(renderer, parent, count, shapeBuilder, builder);

    }

    @Override
    public Node createInstance(RootNode root) {
        MeshNode copy = new MeshNode(root, NodeTypes.meshnode);
        copy.set(this);
        return copy;
    }

    /**
     * Copy values into this node from the source, used when new instance is created
     * 
     * @param source
     */
    public void set(MeshNode source) {
        super.set(source);
        this.shape = source.shape;
    }

    public Shape getShape() {
        return shape;
    }

    @Override
    public NodeRenderer<MeshNode> getNodeRenderer() {
        return MeshNode.nodeRenderer;
    }

    @Override
    public boolean renderNode(NucleusRenderer renderer, Pass currentPass, float[][] matrices) throws GLException {
        NodeRenderer<MeshNode> nodeRenderer = getNodeRenderer();
        if (nodeRenderer != null) {
            nodeRenderer.renderNode(renderer, this, currentPass, matrices);
        }
        return true;
    }

}
