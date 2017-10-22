package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.LineShapeBuilder;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.RectangleShapeBuilder;
import com.nucleus.geometry.ShapeBuilder;
import com.nucleus.shader.ShaderVariables;

/**
 * Contains a mesh that draws lines
 * The intended usage is to put this node in a layer and access it programatically.
 * Default is to draw rectangles
 * This is not performance optimal for many lines, use only if a few lines are drawn in the UI.
 */
public class LineDrawerNode extends Node {

    public enum LineMode {
        RECTANGLE(),
        LINES();
    }

    public static final String LINE_COUNT = "lineCount";
    public static final String LINE_MODE = "lineMode";

    /**
     * xyz for a rectangle
     */
    private float[] rectangleData = new float[3 * 4];

    @SerializedName(LINE_COUNT)
    private int lineCount;
    @SerializedName(LINE_MODE)
    private LineMode lineMode = LineMode.RECTANGLE;

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

    @Override
    public Node createInstance(RootNode root) {
        LineDrawerNode copy = new LineDrawerNode(root);
        copy.set(this);
        return copy;
    }

    public void set(LineDrawerNode source) {
        super.set(source);
        lineCount = source.lineCount;
        lineMode = source.lineMode;
    }

    public ShapeBuilder getShapeBuilder() {
        switch (lineMode) {
            case RECTANGLE:
                return new RectangleShapeBuilder(new RectangleShapeBuilder.RectangleConfiguration(lineCount / 4, 0));
            case LINES:
                return new LineShapeBuilder(lineCount, 0);
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
        Mesh mesh = getMesh(MeshType.MAIN);
        AttributeBuffer vertices = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        int stride = vertices.getFloatStride();
        int startIndex = vertice * stride;
        RectangleShapeBuilder.createQuadArray(values, null, stride, z, rectangleData);
        vertices.setComponents(rectangleData, 3, 0, startIndex, 4);
        mesh.setAttribute4(0, ShaderVariables.aColor, rgba, 4);
    }

    /**
     * Sets data for one line at index
     * 
     * @param vertice
     * @param first x,y,z values for first vertice
     * @param second x,y,z values for second vertice
     * @param rgba Color for 2 vertices
     */
    public void setLine(int vertice, float[] first, float[] second, float[] rgba) {
        Mesh mesh = getMesh(MeshType.MAIN);
        AttributeBuffer vertices = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        int stride = vertices.getFloatStride();
        int startIndex = vertice * stride;
        rectangleData[0] = first[0];
        rectangleData[1] = first[1];
        rectangleData[2] = 1;
        rectangleData[3] = second[0];
        rectangleData[4] = second[1];
        rectangleData[5] = 1;
        vertices.setComponents(rectangleData, 3, 0, startIndex, 2);
        mesh.setAttribute4(vertice, ShaderVariables.aColor, rgba, 2);
    }

}
