package com.nucleus.spirv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import com.nucleus.SimpleLogger;

/**
 * Container for SPIR-V binary
 */
public class SpirvBinary {

    public final static int SPIR_V_MAGIC = 0x07230203;

    public final static int MAGIC_INDEX = 0;
    public final static int VERSION_INDEX = 1;
    public final static int GENERATOR_MAGIC_INDEX = 2;
    public final static int BOUND_INDEX = 3;
    public final static int RESERVED_INDEX = 4;
    public final static int INSTRUCTION_STREAM_INDEX = 5;

    public final ByteBuffer spirv;
    public final int totalWords;

    public SpirvBinary(byte[] spirv) {
        this.spirv = validateSpirv(spirv);
        if (this.spirv == null) {
            throw new IllegalArgumentException("Invalid spirv");
        }
        totalWords = this.spirv.limit() / 4;
    }

    public ByteBuffer getSpirv() {
        return spirv;
    }

    /**
     * Check that byte array starts with spirv magic and that all instruction streams are included.
     * 
     * @param spirv
     * @return The spirv as an int array if data is valid. Null otherwise.
     */
    public static ByteBuffer validateSpirv(byte[] spirvBytes) {
        if (hasSPIRVMagic(spirvBytes, 0, 4)) {
            // Java has BIG_ENDIAN order but the spirv is coming from a byte stream so we need to use LITTLE_ENDIAN
            ByteBuffer bytes = ByteBuffer.allocateDirect(spirvBytes.length).order(ByteOrder.nativeOrder());
            bytes.put(spirvBytes);
            bytes.rewind();
            IntBuffer spirv = bytes.asIntBuffer();
            if (spirv.get(MAGIC_INDEX) == SPIR_V_MAGIC) {
                int version = spirv.get(VERSION_INDEX);
                int wordCount = getTotalWordCount(spirv, INSTRUCTION_STREAM_INDEX);
                bytes.limit((wordCount + INSTRUCTION_STREAM_INDEX) * 4);
                return bytes;
            }
        }
        return null;
    }

    /**
     * Return the wordcount at the specified offset
     * 
     * @param spirv
     * @param offset
     * @return Wordcount att offset or -1 if offset is outside array
     */
    private static int getWordCount(IntBuffer spirv, int offset) {
        if (offset >= spirv.capacity()) {
            return -1;
        }
        int stream = spirv.get(offset);
        return (stream >>> 16) & 0x0ffff;
    }

    /**
     * Returns the wordcount beginning at offset, this will add upp all wordcounts to the end.
     * 
     * @param spirv
     * @param offset
     * @return Total number of words found - or -1 if end of array is reached, this means not enough data in array.
     */
    private static int getTotalWordCount(IntBuffer spirv, int offset) {
        int totalWordCount = 0;
        int wordCount = 0;
        while ((wordCount = getWordCount(spirv, offset)) > 0) {
            totalWordCount += wordCount;
            offset += wordCount;
        }
        // Check if end of array reached
        if (wordCount > 0) {
            return -1;
        }
        SimpleLogger.d(SpirvBinary.class, "Found " + totalWordCount + " instruction words.");
        return totalWordCount;
    }

    /**
     * Returns true if the byte array contains spir-v magic
     * 
     * @param spirv
     * @param offset
     * @param length
     * @return
     */
    public static boolean hasSPIRVMagic(byte[] spirv, int offset, int length) {
        while (offset < (spirv.length - 4) && (length >= 4)) {
            if (spirv[offset] == (SPIR_V_MAGIC & 0xff) &&
                    spirv[offset + 1] == ((SPIR_V_MAGIC >>> 8) & 0xff) &&
                    spirv[offset + 2] == ((SPIR_V_MAGIC >>> 16) & 0xff) &&
                    spirv[offset + 3] == ((SPIR_V_MAGIC >>> 24) & 0xff)) {
                return true;
            }
            offset++;
            length--;
        }
        return false;
    }

}
