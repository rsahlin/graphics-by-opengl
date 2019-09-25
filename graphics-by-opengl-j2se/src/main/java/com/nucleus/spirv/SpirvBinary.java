package com.nucleus.spirv;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.nucleus.SimpleLogger;
import com.nucleus.common.BufferUtils;

/**
 * Container for SPIR-V binary
 */
public class SpirvBinary {

    private static class SpirvInstruction {

        public final static int OpFunctionEnd = 56;

        int wordCount;
        int opCode;

        private SpirvInstruction(int stream) {
            wordCount = (stream >>> 16) & 0x0ffff;
            opCode = stream & 0x0ffff;
        }

        boolean isFunctionEnd() {
            return opCode == OpFunctionEnd;
        }
    }

    private static class SpirvStream {

        int offset;
        int totalWordCount;

        private SpirvStream(int offset) {
            this.offset = offset;
        }

    }

    public final static int SPIR_V_MAGIC = 0x07230203;

    public final static int MAGIC_INDEX = 0;
    public final static int VERSION_INDEX = 1;
    public final static int GENERATOR_MAGIC_INDEX = 2;
    public final static int BOUND_INDEX = 3;
    public final static int RESERVED_INDEX = 4;
    public final static int INSTRUCTION_STREAM_INDEX = 5;

    public final ByteBuffer spirv;
    public final int totalWords;

    public SpirvBinary(ByteBuffer spirv, int totalSpirvWords) {
        this.spirv = createSpirv(spirv, totalSpirvWords);
        if (this.spirv == null) {
            throw new IllegalArgumentException("Invalid spirv");
        }
        totalWords = this.spirv.limit() / 4;
    }

    public ByteBuffer getSpirv() {
        return spirv;
    }

    /**
     * Creates Spirv ByteBuffer from buffer source, the spirv data will be copied into a new buffer.
     * 
     * @param Buffer containing spirv, spirv must start at current buffer position
     * @param total number of 32 bit words in spirv data - this is the size of the complete spirv binary
     * @return Created buffer containing spirv data
     */
    public static ByteBuffer createSpirv(ByteBuffer spirv, int totalSpirvWords) {
        int offset = spirv.position();
        if (SPIRVMagic(spirv) == offset) {
            // Java has BIG_ENDIAN order but the spirv is coming from a byte stream so we need to use LITTLE_ENDIAN
            ByteBuffer bytes = BufferUtils.createByteBuffer(totalSpirvWords * Integer.BYTES);
            bytes.put(spirv);
            bytes.rewind();
            return bytes;
        }
        return null;
    }

    /**
     * Check that byte array starts with spirv magic and that all instruction streams are included.
     * Position is set after last found instruction.
     * 
     * @param Buffer containing spirv, spirv must start at current buffer position
     * @return total number of stream words (32 bits) for the spirv binary (ie total number of 32 bit words in binary),
     * or -1 if not valid spirv
     */
    public static int validateSpirv(ByteBuffer spirv, int endMarker) {
        int offset = spirv.position();
        int totalWords = -1;
        if (SPIRVMagic(spirv) == offset) {
            IntBuffer spirvInt = spirv.asIntBuffer();
            if (spirvInt.get(MAGIC_INDEX) == SPIR_V_MAGIC) {
                int version = spirvInt.get(VERSION_INDEX);
                int bound = spirvInt.get(BOUND_INDEX);
                SimpleLogger.d(SpirvBinary.class,
                        "Spirv version: " + (version >>> 16) + "." + (version & 0x0ffff) + ", bound: " + bound);
                int wordCount = getTotalWordCount(spirvInt, INSTRUCTION_STREAM_INDEX, endMarker);
                if (wordCount > 0) {
                    totalWords = wordCount + INSTRUCTION_STREAM_INDEX;
                    spirv.limit(totalWords * 4);
                    spirv.position(offset + totalWords * Integer.BYTES);
                } else {
                    spirv.position(offset + spirvInt.capacity() * Integer.BYTES);
                }
            }
        }
        return totalWords;
    }

    /**
     * Return the wordcount at the specified offset
     * 
     * @param spirv
     * @param stream
     * @return Wordcount att offset or -1 if offset is outside array
     */
    private static SpirvInstruction getWordCount(IntBuffer spirv, SpirvStream stream) {
        if (stream.offset >= spirv.capacity()) {
            return null;
        }
        int read = spirv.get(stream.offset);
        SpirvInstruction instruction = new SpirvInstruction(read);
        stream.totalWordCount += instruction.wordCount;
        stream.offset += instruction.wordCount;
        return instruction;

    }

    /**
     * Check if offset is end marker
     * 
     * @param spirv
     * @param offset
     * @return true of offset has end marker
     */
    private static boolean isEndMarker(IntBuffer spirv, int offset, int marker) {
        return (spirv.get(offset) == marker);
    }

    /**
     * Returns the wordcount beginning at offset, this will add upp all wordcounts to the end.
     * 
     * @param spirv
     * @param offset
     * @return Total number of words found - or -1 if end of array is reached, this means not enough data in array.
     */
    private static int getTotalWordCount(IntBuffer spirv, int offset, int endMarker) {
        int totalWordCount = 0;
        SpirvStream stream = new SpirvStream(offset);
        SpirvInstruction instruction = null;
        while ((instruction = getWordCount(spirv, stream)) != null) {
            SimpleLogger.d(SpirvBinary.class,
                    "wordCount: " + instruction.wordCount + ", opCode: " + instruction.opCode + ", offset: "
                            + stream.offset);
            if (instruction.isFunctionEnd() && isEndMarker(spirv, stream.offset, endMarker)) {
                // Found end of spirv
                SimpleLogger.d(SpirvBinary.class, "Found " + totalWordCount + " instruction words.");
                return totalWordCount;
            }
        }
        return instruction == null ? -1 : stream.totalWordCount;
    }

    /**
     * Returns offset to SPIRV magic offset or -1 if not found
     * 
     * @param spirv
     * @param offset Offset into SPIRV where to look for magic
     * @param length Max length of spirv to check, including offset. length - offset bytes will be checked
     * @return Total offset, from beginning of spirv, of magic. -1 if not found
     */
    public static int SPIRVMagic(byte[] spirv, int offset, int length) {
        while (offset < (spirv.length - 4) && (length >= 4)) {
            if (spirv[offset] == (SPIR_V_MAGIC & 0xff) &&
                    spirv[offset + 1] == ((SPIR_V_MAGIC >>> 8) & 0xff) &&
                    spirv[offset + 2] == ((SPIR_V_MAGIC >>> 16) & 0xff) &&
                    spirv[offset + 3] == ((SPIR_V_MAGIC >>> 24) & 0xff)) {
                return offset;
            }
            offset++;
            length--;
        }
        return -1;
    }

    /**
     * Returns offset to SPIRV magic offset or -1 if not found.
     * If magic is found the position is updated to start of SPIR_V_MAGIC, if magic not found position remains
     * unchanged
     * 
     * @param buffer
     * @return Total offset, from beginning of buffer, of magic. -1 if not found
     */
    public static int SPIRVMagic(ByteBuffer buffer) {
        int offset = buffer.position();
        int remaining = buffer.remaining();
        SimpleLogger.d(SpirvBinary.class,
                "Checking spirv magic, buffer position: " + buffer.position() + ", limit: " + buffer.limit());
        while (remaining >= Integer.BYTES) {
            if (buffer.get(offset) == (SPIR_V_MAGIC & 0xff) &&
                    buffer.get(offset + 1) == ((SPIR_V_MAGIC >>> 8) & 0xff) &&
                    buffer.get(offset + 2) == ((SPIR_V_MAGIC >>> 16) & 0xff) &&
                    buffer.get(offset + 3) == ((SPIR_V_MAGIC >>> 24) & 0xff)) {
                buffer.position(offset);
                return offset;
            }
            offset++;
            remaining--;
        }
        return -1;
    }
}
