package com.nucleus.scene;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.common.Constants;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.Mesh.Mode;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.shader.GenericShaderProgram;
import com.nucleus.shader.ShaderProgram.ProgramType;
import com.nucleus.shader.TranslateProgram;
import com.nucleus.shader.VariableIndexer.Indexer;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureType;

/**
 * Contains a mesh that draws lines or points
 * The intended usage is to put this node in a layer and access it programatically.
 * Default is to draw rectangles
 * This is not performance optimal for many lines, use only if a few lines are drawn in the UI.
 */
public class LineDrawerNode extends Node implements AttributeUpdater.Consumer {

    public enum LineMode {
        RECTANGLE(),
        LINES(),
        LINE_STRIP(),
        POINTS();
    }

    public static final String LINE_COUNT = "lineCount";
    public static final String LINE_MODE = "lineMode";
    public static final String POINT_SIZE = "pointSize";

    @SerializedName(LINE_COUNT)
    private int lineCount;
    @SerializedName(LINE_MODE)
    private LineMode lineMode = LineMode.RECTANGLE;
    @SerializedName(POINT_SIZE)
    private float pointSize = 1;

    transient private float[] attributes;
    transient private boolean attributesDirty = false;
    transient AttributeBuffer buffer;
    /**
     * If drawcount is updated it must be set to buffer in {@link #updateAttributeData()} method.
     */
    transient int drawCount = Constants.NO_VALUE;
    transient int drawOffset = Constants.NO_VALUE;

    /**
     * Creates a nodebuilder that can be used to create LineDrawerNodes with mesh(es), that can be used to draw points
     * or lines.
     * Use for instance when node is created programatically.
     * 
     * @param renderer
     * @param nodeBuilder
     * @param vertices Number of vertices in mesh
     * @param meshCount Number of meshes to create
     * @param mode Mesh drawmode
     * @return
     */
    public static Node.Builder<Node> createBuilder(NucleusRenderer renderer, Node.Builder<Node> nodeBuilder,
            int vertices, int meshCount, Mode mode) {
        nodeBuilder.setType(NodeTypes.linedrawernode);
        TranslateProgram program = (TranslateProgram) AssetManager.getInstance()
                .getProgram(renderer.getGLES(), new TranslateProgram(Shading.flat));
        nodeBuilder.setProgram(program);
        com.nucleus.geometry.Mesh.Builder<Mesh> pointMeshBuilder = Mesh.createBuilder(renderer, vertices,
                new Material(),
                program, TextureFactory.createTexture(TextureType.Untextured), null, mode);
        nodeBuilder.setMeshBuilder(pointMeshBuilder).setMeshCount(meshCount);
        return nodeBuilder;
    }

    @Override
    public Mesh.Builder<Mesh> createMeshBuilder(NucleusRenderer renderer, Node parent, int count,
            ShapeBuilder shapeBuilder) throws IOException {
        LineDrawerNode lineParent = (LineDrawerNode) parent;
        Mesh.Builder<Mesh> builder = new Mesh.Builder<>(renderer);
        switch (lineParent.getLineMode()) {
            case LINES:
                builder.setArrayMode(Mode.LINES, count * 2, 0);
                break;
            case LINE_STRIP:
                builder.setArrayMode(Mode.LINE_STRIP, count * 2, 0);
                break;
            case POINTS:
                builder.setArrayMode(Mode.POINTS, count, 0);
                break;
            case RECTANGLE:
                // Rectangle shares vertices, 4 vertices per rectangle
                builder.setElementMode(Mode.LINES, count, 0, count * 2);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for mode " + lineParent.getLineMode());
        }
        Texture2D tex = TextureFactory.createTexture(TextureType.Untextured);
        builder.setTexture(tex);
        if (parent.getProgram() == null) {
            parent.setProgram(
                    AssetManager.getInstance().getProgram(renderer.getGLES(),
                            new GenericShaderProgram(new String[] { "flatline", "flatline" },
                                    ProgramType.VERTEX_FRAGMENT)));
        }
        return initMeshBuilder(renderer, parent, count, lineParent.getShapeBuilder(), builder);
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
    public LineMode getLineMode() {
        return lineMode;
    }

    /**
     * Returns size to use for points and lines
     * 
     * @return
     */
    public float getPointSize() {
        return pointSize;
    }

    /**
     * Sets size used for points and lines
     * 
     * @param pointSize
     */
    public void setPointSize(float pointSize) {
        this.pointSize = pointSize;
    }

    @Override
    public Node createInstance(RootNode root) {
        LineDrawerNode copy = new LineDrawerNode(root);
        copy.set(this);
        return copy;
    }

    @Override
    public void create() {
        Mesh mesh = getMesh(MeshIndex.MAIN);
        mesh.setAttributeUpdater(this);
        bindAttributeBuffer(mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES));
        mapper = new Indexer(program);
    }

    public void set(LineDrawerNode source) {
        super.set(source);
        lineCount = source.lineCount;
        lineMode = source.lineMode;
        pointSize = source.pointSize;
    }

    public ShapeBuilder getShapeBuilder() {
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
        int translate = mapper.vertex;
        int color = mapper.emissive;
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
        internalSetVertex(offset + mapper.vertex, offset + mapper.albedo, next, z, rgba);
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
