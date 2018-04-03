package com.nucleus.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for input/output stream operations.
 * 
 * @author Richard Sahlin
 *
 */
public class StreamUtils {

    private final static int DEFAULT_BUFFER_SIZE = 2048;

    /**
     * Encapsulates data read from BufferedInputStream
     *
     */
    public class InputData {
        private byte[] buffer;
        private int count;

        /**
         * 
         * @param buffer The input data, eg read from inputstream.
         * @param count Number of bytes of valid data, may be smaller than length if not all of buffer is filled.
         */
        public InputData(byte[] buffer, int count) {
            this.buffer = buffer;
            this.count = count;
        }

        /**
         * Returns the buffer data
         * 
         * @return
         */
        public byte[] getBuffer() {
            return buffer;
        }

        /**
         * Returns the number of bytes of valid data in buffer, may be smaller than length.
         * 
         * @return
         */
        public int getCount() {
            return count;
        }
    }

    /**
     * Utility method to read data from inputstream using buffered inputstream.
     * The default buffer size will be used.
     * This is the same as calling readFromStream(InputStream, DEFAULT_BUFFER_SIZE)
     * 
     * @param in
     * @return
     * @throws IOException
     */
    public static byte[] readFromStream(InputStream in) throws IOException {
        return readFromStream(in, DEFAULT_BUFFER_SIZE);

    }

    /**
     * Utility method to read data from inputstream and return as a UTF-8 encoded
     * String.
     * Use this method when reading Strings from file to get correct encoding.
     * 
     * @param in
     * @return
     * @throws IOException
     */
    public static String readStringFromStream(InputStream in) throws IOException {
        // TODO - If Android build version => 19 then java.nio.StandardCharset can be used
        return new String(readFromStream(in), "UTF-8");
    }

    /**
     * Utility method to read data from inputstream using buffered inputstream.
     * The buffer size used when reading data is specified.
     * 
     * @param in
     * @param buffersize Size of buffer to use when reading data, for larger files it may be optimal to increase
     * the size. A value of 8K should be ok for normal file sizes.
     * @return
     * @throws IOException
     */
    public static byte[] readFromStream(InputStream in, int buffersize) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        List<InputData> buffers = new ArrayList<InputData>();
        int totalCount = 0;
        int count = 0;
        byte[] buffer = new byte[buffersize];
        StreamUtils util = new StreamUtils();
        while ((count = bis.read(buffer)) != -1) {
            totalCount += count;
            buffers.add(util.new InputData(buffer, count));
            buffer = new byte[buffersize];
        }
        byte[] data = new byte[totalCount];
        int index = 0;
        for (InputData b : buffers) {
            System.arraycopy(b.getBuffer(), 0, data, index, b.getCount());
            index += b.getCount();
        }
        return data;
    }
}
