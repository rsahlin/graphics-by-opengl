package com.nucleus.vulkan.structs;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import com.nucleus.Backend;
import com.nucleus.io.StreamUtils;
import com.nucleus.shader.Shader.Categorizer;
import com.nucleus.shader.Shader.ShaderType;
import com.nucleus.shader.ShaderBinary;

/**
 * Wrapper for VkShaderModuleCreateInfo
 *
 */
public class ShaderModuleCreateInfo extends ShaderBinary {

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
    private int flags = 0;

    private int size;

    private int position;

    private ByteBuffer data;

    public ShaderModuleCreateInfo(String path, String sourcename, String suffix, ShaderType type) {
        super(path, sourcename, suffix, type);
    }

    /**
     * Returns the bytebuffer containing shader binary data, with the position of
     * the buffer set to point to the first data.
     * 
     * @return The buffer containing data with the proper position, reading data at
     * current position will get the correct data.
     */
    public ByteBuffer getBuffer() {
        data.position(position);
        return data;
    }

    @Override
    public void loadShader(Backend backend, Categorizer function) throws IOException {
        try {
            data = StreamUtils.readBufferFromName(getFullSourceName());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

}
