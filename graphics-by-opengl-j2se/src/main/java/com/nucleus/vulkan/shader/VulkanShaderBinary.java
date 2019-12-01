package com.nucleus.vulkan.shader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import com.nucleus.Backend;
import com.nucleus.io.StreamUtils;
import com.nucleus.shader.Shader.Categorizer;
import com.nucleus.shader.Shader.ShaderType;
import com.nucleus.shader.ShaderBinary;

public class VulkanShaderBinary extends ShaderBinary {

    public enum Type {
        VERTEX("vert", "_vert.spv"),
        TESSELATION_CONTROL("tesc", "_tesc.spv"),
        TESSELATION("tese", "_tese.spv"),
        GEOMETRY("geom", "_geom.spv"),
        FRAGMENT("frag", "_frag.spv"),
        COMPUTE("comp", "_comp.spv");

        public final String stage;
        public final String fileName;

        Type(String stage, String fileName) {
            this.stage = stage;
            this.fileName = fileName;
        }
    }

    private ByteBuffer data;

    public VulkanShaderBinary(String path, String sourcename, String suffix, ShaderType type) {
        super(path, sourcename, suffix, type);
    }

    /**
     * Returns the bytebuffer containing shader binary data.
     * 
     * @return The shader code
     */
    public ByteBuffer getBuffer() {
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
