package com.nucleus.spirv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.nucleus.SimpleLogger;
import com.nucleus.common.FileUtils;
import com.nucleus.io.StreamUtils;

/**
 * Used to load spirv binary from a stream.
 * 
 */
public class SpirvLoader {

    int offset = 0;
    int totalWords = 0;

    /**
     * Searches for the spirv magic number, then loads data until end of spirv word stream
     * 
     * @param inputstream
     * @param Data loaded into this buffer at current position
     * @param Max number of millis to wait for data to become available when reading
     */
    public void loadSpirv(InputStream stream, ByteBuffer buffer, int readTimeoutMillis) throws IOException {
        offset = 0;
        totalWords = 0;
        // Find spir-v magic number
        while ((offset = SpirvBinary.SPIRVMagic(buffer)) < 0
                || (totalWords = SpirvBinary.validateSpirv(buffer)) < 0) {
            int len = FileUtils.getInstance().waitForAvailable(stream, readTimeoutMillis);
            if (len <= 0) {
                throw new IllegalArgumentException("No data to read");
            }
            int offset = buffer.position();
            len = StreamUtils.readFromStream(stream, buffer, len);
            buffer.flip();
            buffer.position(offset);
            SimpleLogger.d(getClass(), "Loaded:" + StandardCharsets.ISO_8859_1.decode(buffer));
            buffer.clear();
        }
        SimpleLogger.d(getClass(), "Loaded spirv at offset " + offset + ", with " + totalWords + " words");
    }

    public int getOffset() {
        return offset;
    }

    public int getTotalWords() {
        return totalWords;
    }

}
