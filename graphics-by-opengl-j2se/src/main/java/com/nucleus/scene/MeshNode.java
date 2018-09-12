package com.nucleus.scene;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.Mode;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.RectangleShapeBuilder;
import com.nucleus.geometry.shape.RectangleShapeBuilder.RectangleConfiguration;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.geometry.shape.ShapeBuilderFactory;
import com.nucleus.renderer.MeshRenderer;
import com.nucleus.renderer.NucleusMeshRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureType;
import com.nucleus.vecmath.Shape;

/**
 * Node for a custom mesh, the mesh can either be created programmatically in runtime or by using a custom
 * implementation of MeshFactory when node is serialized.
 *
 */
public class MeshNode extends AbstractMeshNode<Mesh> {

    transient protected static MeshRenderer<Mesh> meshRenderer;
    
    /**
     * If defined this is the shape of the mesh - if not specified a fullscreen 2D rect will be created.
     */
    @SerializedName(Shape.SHAPE)
    protected Shape shape;

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    protected MeshNode() {
        super();
    }

    protected MeshNode(RootNode root, Type<Node> type) {
        super(root, type);
    }

    @Override
    public MeshBuilder<Mesh> createMeshBuilder(NucleusRenderer renderer, ShapeBuilder shapeBuilder)
            throws IOException {
        int count = 1;
        Mesh.Builder<Mesh> builder = new Mesh.Builder<>(renderer);
        Texture2D tex = null;
        if (getTextureRef() == null) {
            tex = TextureFactory.createTexture(TextureType.Untextured);

        } else {
            tex = AssetManager.getInstance().getTexture(renderer, getTextureRef());
        }
        builder.setTexture(tex);
        if (shapeBuilder == null) {
            LayerNode layer = getRootNode().getLayerNode(null);
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
        return initMeshBuilder(renderer, count, shapeBuilder, builder);

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
    public void create() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void createMeshRenderer() {
        if (meshRenderer == null) {
            meshRenderer = new NucleusMeshRenderer();
        }
    }
    
    @Override
    public MeshRenderer<Mesh> getMeshRenderer() {
        return meshRenderer;
    }
    
}
