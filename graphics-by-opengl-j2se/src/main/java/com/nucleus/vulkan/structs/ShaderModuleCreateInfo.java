package com.nucleus.vulkan.structs;

import java.nio.ByteBuffer;

/**
 * Wrapper for VkShaderModuleCreateInfo
 *
 */
public class ShaderModuleCreateInfo {

    public enum Type {
        VERTEX(),
        FRAGMENT(),
        GEOMETRY(),
        TESSELATION(),
        COMPUTE();
    }

    /**
     * Not used
     */
    private final int flags = 0;

    private final int size;

    private final int position;

    private final ByteBuffer data;

    private final Type type;

    public ShaderModuleCreateInfo(String spirvName, Type type) {
        this.type = type;
        data = null;
        size = 0;
        position = 0;
    }

    /**
     * Creates a new shader binary container with the contents of the buffer.
     * Size will be set to capacity
     * position will be set to 0
     * 
     * @param data
     * @param type The type of shader module
     */
    public ShaderModuleCreateInfo(ByteBuffer data, Type type) {
        this.size = data.capacity();
        this.position = 0;
        this.data = data;
        this.type = type;
    }

    /**
     * Returns the bytebuffer containing shader binary data, with the position of the buffer set to point
     * to the first data.
     * 
     * @return The buffer containing data with the proper position, reading data at current position will get
     * the correct data.
     */
    public ByteBuffer getBuffer() {
        data.position(position);
        return data;
    }

}
