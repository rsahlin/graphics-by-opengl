package com.nucleus.vulkan;

public class Extent3D {
    final int[] values = new int[3];

    public Extent3D(int width, int height, int depth) {
        values[0] = width;
        values[1] = height;
        values[2] = depth;
    }
}