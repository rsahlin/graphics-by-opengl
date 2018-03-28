package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.Constants;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.RectangleShapeBuilder;
import com.nucleus.geometry.ShapeBuilder;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.shader.CommonShaderVariables;

/**
 * Contains a mesh that draws lines
 * The intended usage is to put this node in a layer and access it programatically.
 * Default is to draw rectangles
 * This is not performance optimal for many lines, use only if a few lines are drawn in the UI.
 */
public class LineDrawerNode extends Node implements AttributeUpdater.Consumer {

    public enum LineMode {
        RECTANGLE(),
        LINES(),
        LINE_STRIP();
    }

    public static final String LINE_COUNT = "lineCount";
    public static final String LINE_MODE = "lineMode";
    public static final String LINE_WIDTH = "lineWidth";

    @SerializedName(LINE_COUNT)
    private int lineCount;
    @SerializedName(LINE_MODE)
    private LineMode lineMode = LineMode.RECTANGLE;
    @SerializedName(LINE_WIDTH)
    private float lineWidth = 1;

    transient private float[] attributes;
    transient private boolean attributesDirty = false;
    transient AttributeBuffer buffer;
    /**
     * If drawcount is updated it must be set to buffer in {@link #updateAttributeData()} method.
     */
    transient int drawCount = Constants.NO_VALUE;
    transient int drawOffset = Constants.NO_VALUE;

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
     * Returns the linewidth
     * 
     * @return
     */
    public float getLineWidth() {
        return lineWidth;
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
    }

    public void set(LineDrawerNode source) {
        super.set(source);
        lineCount = source.lineCount;
        lineMode = source.lineMode;
        lineWidth = source.lineWidth;
    }

    public ShapeBuilder getShapeBuilder() {
        switch (lineMode) {
            case RECTANGLE:
                return new RectangleShapeBuilder(new RectangleShapeBuilder.RectangleConfiguration(lineCount / 4, 0));
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
        int translate = getMesh(MeshIndex.MAIN).getMaterial().getProgram()
                .getShaderVariable(CommonShaderVariables.aTranslate)
                .getOffset();
        int color = getMesh(MeshIndex.MAIN).getMaterial().getProgram().getShaderVariable(CommonShaderVariables.aColor)
                .getOffset();
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
     * Sets data for one line at index, this sets first and second positions.
     * Suitable for LINES mode.
     * 
     * @param vertice
     * @param first x,y,z values for first vertice
     * @param second x,y,z values for second vertice
     * @param z
     * @param rgba Color for 2 vertices
     */
    public void setLine(int vertice, float[] first, float[] second, float z, float[] rgba) {
        int offset = buffer.getFloatStride() * vertice;
        int translate = getMesh(MeshIndex.MAIN).getMaterial().getProgram()
                .getShaderVariable(CommonShaderVariables.aTranslate)
                .getOffset();
        int color = getMesh(MeshIndex.MAIN).getMaterial().getProgram().getShaderVariable(CommonShaderVariables.aColor)
                .getOffset();
        internalSetVertex(offset + translate, offset + color, first, z, rgba);
        offset += buffer.getFloatStride();
        internalSetVertex(offset + translate, offset + color, second, z, rgba);
    }

    /**
     * Adds a vertex for next line, this is for LINE_STRIP mode
     * 
     * @param vertice
     * @param next
     * @param z
     * @param rgba
     */
    public void addLine(int vertice, float[] next, float z, float[] rgba) {
        int offset = buffer.getFloatStride() * vertice;
        int translate = getMesh(MeshIndex.MAIN).getMaterial().getProgram()
                .getShaderVariable(CommonShaderVariables.aTranslate)
                .getOffset();
        int color = getMesh(MeshIndex.MAIN).getMaterial().getProgram().getShaderVariable(CommonShaderVariables.aColor)
                .getOffset();
        internalSetVertex(offset + translate, offset + color, next, z, rgba);
    }

    /**
     * Updated the position of a vertice in the line array
     * 
     * @param vertice
     * @param pos
     * @param z
     */
    public void setPos(int vertice, float[] pos, float z) {
        int offset = buffer.getFloatStride() * vertice;
        int translate = getMesh(MeshIndex.MAIN).getMaterial().getProgram()
                .getShaderVariable(CommonShaderVariables.aTranslate)
                .getOffset();
        internalSetVertex(offset + translate, pos, z);

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

    private void internalSetVertex(int translate, float[] pos, float z) {
        attributes[translate++] = pos[0];
        attributes[translate++] = pos[1];
        attributes[translate] = z;
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
