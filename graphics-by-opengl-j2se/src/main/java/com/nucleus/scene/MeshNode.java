package com.nucleus.scene;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.Mesh.Mode;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.shape.RectangleShapeBuilder;
import com.nucleus.geometry.shape.RectangleShapeBuilder.RectangleConfiguration;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.geometry.shape.ShapeBuilderFactory;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.NodeRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureType;
import com.nucleus.vecmath.Shape;

/**
 * Node for a custom mesh, the mesh can either be created programmatically in runtime or by using a custom
 * implementation of MeshFactory when node is serialized.
 *
 */
public class MeshNode extends NucleusMeshNode<Mesh> {

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
    public void renderMesh(NucleusRenderer renderer, ShaderProgram program, Mesh mesh, float[][] matrices)
            throws GLException {
        GLES20Wrapper gles = renderer.getGLES();
        Consumer updater = mesh.getAttributeConsumer();
        if (updater != null) {
            updater.updateAttributeData(renderer);
        }
        if (mesh.getDrawCount() == 0) {
            return;
        }
        Material material = mesh.getMaterial();

        program.updateAttributes(gles, mesh);
        program.updateUniforms(gles, matrices, mesh);
        program.prepareTextures(gles, mesh);

        material.setBlendModeSeparate(gles);

        ElementBuffer indices = mesh.getElementBuffer();

        if (indices == null) {
            gles.glDrawArrays(mesh.getMode().mode, mesh.getOffset(), mesh.getDrawCount());
            GLUtils.handleError(gles, "glDrawArrays ");
            timeKeeper.addDrawArrays(mesh.getDrawCount());
        } else {
            if (indices.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
                gles.glDrawElements(mesh.getMode().mode, mesh.getDrawCount(), indices.getType().type,
                        mesh.getOffset());
                GLUtils.handleError(gles, "glDrawElements with ElementBuffer ");
            } else {
                gles.glDrawElements(mesh.getMode().mode, mesh.getDrawCount(), indices.getType().type,
                        indices.getBuffer().position(mesh.getOffset()));
                GLUtils.handleError(gles, "glDrawElements no ElementBuffer ");
            }
            AttributeBuffer vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES_STATIC);
            if (vertices == null) {
                vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES);
            }
            timeKeeper.addDrawElements(vertices.getVerticeCount(), mesh.getDrawCount());
        }

    }

}
