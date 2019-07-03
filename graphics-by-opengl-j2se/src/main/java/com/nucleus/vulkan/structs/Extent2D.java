package com.nucleus.vulkan.structs;

public class Extent2D {

    public final int[] values = new int[2];

    public Extent2D() {

    }

    public Extent2D(int width, int height) {
        values[0] = width;
        values[1] = height;
    }

    public int getWidth() {
        return values[0];
    }

    public void setWidth(int width) {
        values[0] = width;
    }

    public int getHeight() {
        return values[1];
    }

    public void setHeight(int height) {
        values[1] = height;
    }

    @Override
    public String toString() {
        return "Size " + values[0] + ", " + values[1];
    }

}
