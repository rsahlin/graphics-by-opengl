package com.nucleus.scene;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.nucleus.Backend.DrawMode;
import com.nucleus.GraphicsPipeline;
import com.nucleus.common.Constants;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.opengl.geometry.GLMesh;
import com.nucleus.opengl.shader.GLShaderProgram;
import com.nucleus.opengl.shader.GLShaderProgram.ProgramType;
import com.nucleus.opengl.shader.GenericShaderProgram;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.shader.Indexer;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureType;

/**
 * Contains a mesh that draws lines or points, lines or points can be made into shapes by
 * using a shapebuilder.
 * The intended usage is to put this node in a layer and access it programatically or to specify
 * one of the predefined shapes.
 */
public class LineDrawerNode extends AbstractMeshNode<Mesh> implements AttributeUpdater.Consumer {

    protected final static String VERTEX_SHADER_NAME = "flatline";
    protected final static String FRAGMENT_SHADER_NAME = "flatline";

    public static final String LINE_COUNT = "lineCount";
    public static final String LINE_MODE = "lineMode";

    @SerializedName(LINE_COUNT)
    private int lineCount;
    @SerializedName(LINE_MODE)
    private DrawMode lineMode;

    transient private float[] attributes;
    transient private boolean attributesDirty = false;
    transient AttributeBuffer buffer;
    /**
     * If drawcount is updated it must be set to buffer in {@link #updateAttributeData()} method.
     */
    transient int drawCount = Constants.NO_VALUE;
    transient int drawOffset = Constants.NO_VALUE;
    transient Indexer indexer;

    @Override
    public MeshBuilder<Mesh> createMeshBuilder(NucleusRenderer renderer, ShapeBuilder shapeBuilder)
            throws IOException {
        int count = getLineCount();
        MeshBuilder<Mesh> builder = new GLMesh.Builder<>(renderer);
        builder.setShapeBuilder(shapeBuilder);
        switch (getLineMode()) {
            case LINES:
                builder.setArrayMode(DrawMode.LINES, count * 2, 0);
                break;
            case LINE_STRIP:
                builder.setArrayMode(DrawMode.LINE_STRIP, count + 1, 0);
                break;
            case POINTS:
                builder.setArrayMode(DrawMode.POINTS, count, 0);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for mode " + getLineMode());
        }
        if (builder.getShapeBuilder() == null) {
            builder.setShapeBuilder(createShapeBuilder(builder));
        }
        Texture2D tex = TextureFactory.getInstance().createTexture(TextureType.Untextured);
        builder.setTexture(tex);
        if (getPipeline() == null) {
            GraphicsPipeline pipeline = renderer.getAssets().getPipeline(renderer,
                    new GenericShaderProgram(new String[] { VERTEX_SHADER_NAME, FRAGMENT_SHADER_NAME }, null,
                            GLShaderProgram.Shading.flat, null,
                            ProgramType.VERTEX_FRAGMENT));
            setPipeline(pipeline);
        }
        return initMeshBuilder(renderer, count, builder.getShapeBuilder(), builder);
    }

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    @Deprecated
    protected LineDrawerNode() {
        super();
    }

    /**
     * Creates an empty node, add children and meshes as needed.
     * 
     * @param root
     */
    protected LineDrawerNode(RootNode root) {
        super(root, NodeTypes.linedrawernode);
        setRootNode(root);
    }

    /**
     * Returns the number of lines in this node
     * 
     * @return
     */
    public int getLineCount() {
        return lineCount;
    }

    /**
     * Returns the linderawer mode
     * 
     * @return
     */
    public DrawMode getLineMode() {
        return lineMode;
    }

    @Override
    public Node createInstance(RootNode root) {
        LineDrawerNode copy = new LineDrawerNode(root);
        copy.set(this);
        return copy;
    }

    @Override
    public void createTransient() {
    }

    @Override
    public void onCreated() {
        Mesh mesh = getMesh(MeshIndex.MAIN);
        mesh.setAttributeUpdater(this);
        bindAttributeBuffer(mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES));
        // indexer = new Indexer(this.p);
        throw new IllegalArgumentException("Not implemented");
    }

    public void set(LineDrawerNode source) {
        super.set(source);
        lineCount = source.lineCount;
        lineMode = source.lineMode;
    }

    private ShapeBuilder createShapeBuilder(MeshBuilder<Mesh> meshBuilder) {
        switch (lineMode) {
            case LINES:
            case LINE_STRIP:
                return null;
            default:
                throw new IllegalArgumentException("Not implemented for " + lineMode);
        }
    }

    /**
     * Sets a rectangle to be drawn, this node must be setup with RECTANGLE mode for this call to work.
     * TODO : Should this method also set the indices in the elementbuffer?
     * 
     * @param vertice
     * @param values x,y, width and height
     * @param z
     * @param rgba Color for each vertex
     */
    public void setRectangle(int vertice, float[] values, float z, float[] rgba) {
        int offset = buffer.getFloatStride() * vertice;
        int translate = indexer.vertex;
        int color = indexer.emissive;
        float[] pos = new float[2];
        internalSetVertex(offset + translate, offset + color, copy(values, 0, pos), z, rgba);
        internalSetVertex(offset + translate, offset + color, copy(values, 1, pos), z, rgba);
        internalSetVertex(offset + translate, offset + color, copy(values, 2, pos), z, rgba);
        internalSetVertex(offset + translate, offset + color, copy(values, 3, pos), z, rgba);

    }

    private float[] copy(float[] rect, int index, float[] dest) {
        switch (index) {
            case 0:
                dest[0] = rect[0];
                dest[1] = rect[1];
                return dest;
            case 1:
                dest[0] = rect[0] + rect[2];
                dest[1] = rect[1];
                return dest;
            case 2:
                dest[0] = rect[0] + rect[2];
                dest[1] = rect[1] + rect[3];
                return dest;
            case 3:
                dest[0] = rect[0];
                dest[1] = rect[1] + rect[3];
                return dest;
        }
        return null;
    }

    /**
     * Adds a vertex at the offset for vertice.
     * Depending on mode this can be used to add a point or line.
     * The caller must update the drawcount for the change to be visible
     * Before calling this method, an AttributeBuffer must be connected to this node - this is normally done
     * when the node factory is configure with a mesh builder.
     * 
     * @param vertice The vertex number to add.
     * @param pos Position of vertex (xy)
     * @param z z position to use
     * @param rgba Colour
     */
    public void addVertex(int vertice, float[] next, float z, float[] rgba) {
        int offset = buffer.getFloatStride() * vertice;
        internalSetVertex(offset + indexer.vertex, offset + indexer.albedo, next, z, rgba);
    }

    private void internalSetVertex(int translate, int color, float[] pos, float z, float[] rgba) {
        attributes[translate++] = pos[0];
        attributes[translate++] = pos[1];
        attributes[translate] = z;
        attributes[color++] = rgba[0];
        attributes[color++] = rgba[1];
        attributes[color++] = rgba[2];
        attributes[color] = rgba[3];
        attributesDirty = true;
    }

    @Override
    public void updateAttributeData(NucleusRenderer renderer) {
        if (attributes == null || buffer == null) {
            throw new IllegalArgumentException(Consumer.BUFFER_NOT_BOUND);
        }
        if (drawCount != Constants.NO_VALUE) {
            updateDrawCount(drawCount, drawOffset);
            drawCount = Constants.NO_VALUE;
            drawOffset = Constants.NO_VALUE;
            attributesDirty = true;
        }
        if (attributesDirty) {
            buffer.setArray(attributes, 0, 0, attributes.length);
            buffer.setDirty(true);
            attributesDirty = false;
        }
    }

    @Override
    public void bindAttributeBuffer(AttributeBuffer buffer) {
        attributes = new float[buffer.getCapacity()];
        this.buffer = buffer;
    }

    /**
     * This method must be called from {@link #updateAttributeData()}
     * 
     * @param count
     * @param offset
     */
    private void updateDrawCount(int count, int offset) {
        Mesh mesh = getMesh(MeshIndex.MAIN);
        mesh.setDrawCount(count, offset);
    }

    /**
     * Sets the number of vertices to draw and offset
     * 
     * @param count
     * @param offset
     */
    public void setDrawCount(int count, int offset) {
        drawCount = count;
        drawOffset = offset;
    }

}
