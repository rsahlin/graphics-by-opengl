package com.nucleus.scene.gltf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.google.gson.annotations.SerializedName;
import com.nucleus.SimpleLogger;
import com.nucleus.common.BufferUtils;

/**
 * The Buffer as it is loaded using the glTF format.
 * 
 * A buffer points to binary geometry, animation, or skins.
 * 
 * Properties
 * 
 * Type Description Required
 * uri string The uri of the buffer. No
 * byteLength integer The length of the buffer in bytes. ✅ Yes
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 * 
 * This class can be serialized using gson
 */

public class Buffer extends GLTFNamedValue {

    /**
     * Max number of dataelements to print from buffer in toString()
     */
    public static final int MAX_BUFFER_PRINT = 100;

    private static final String URI = "uri";
    private static final String BYTE_LENGTH = "byteLength";

    @SerializedName(URI)
    private String uri;
    @SerializedName(BYTE_LENGTH)
    private int byteLength;

    transient ByteBuffer buffer;
    transient int bufferName;

    /**
     * Creates a new buffer with the specified byteLength - the buffer will be created by calling
     * {@link #createBuffer()}
     * 
     * @param name Name of the buffer
     * @param byteLength
     */
    public Buffer(String name, int byteLength) {
        this.name = name;
        this.byteLength = byteLength;
        createBuffer();
    }

    public String getUri() {
        return uri;
    }

    public int getByteLength() {
        return byteLength;
    }

    /**
     * Returns the underlying ByteBuffer, or null if serialized and not called {@link #createBuffer()}
     * Deprecated use {@link Accessor#getBuffer()} instead
     * 
     * @return
     */
    @Deprecated
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Sets the buffer object to use, this must be allocated by GL, or 0 to disable buffer objects.
     * 
     * @param bufferName Buffer name or 0 to disable
     */
    public void setBufferName(int bufferName) {
        this.bufferName = bufferName;
    }

    /**
     * Returns the buffer object name, if not 0 then use the buffer object when sending data to GL.
     * 
     * @return
     */
    public int getBufferName() {
        return bufferName;
    }

    /**
     * Creates the buffer for the storage.
     * 
     * @throws IllegalArgumentException If buffer has already been created, and not destroyed
     * 
     */
    public void createBuffer() {
        if (buffer != null) {
            throw new IllegalArgumentException("Buffer already created");
        }
        SimpleLogger.d(getClass(), "Creating buffer with byte size: " + byteLength);
        buffer = BufferUtils.createByteBuffer(byteLength);
    }

    /**
     * Stores the float array at position
     * 
     * @param floatData
     * @param position Position, in floats, where to start storing data
     */
    public void put(float[] floatData, int position) {
        FloatBuffer fb = buffer.asFloatBuffer();
        fb.position(position);
        fb.put(floatData);
    }

    /**
     * Copies the contents of the bufferview in the source into the current position of this buffer.
     * Copy will use bytestride of source and copy tighly packed into this buffer.
     * Use if data should be packed into this buffer.
     * @param source
     */
    public void put(Accessor source) {
        BufferView view = source.getBufferView();
        ByteBuffer sourceBuffer = source.getBuffer();
        int limit = sourceBuffer.limit();
        if (view.getByteStride() <= source.getComponentType().size * source.getType().size) {
            sourceBuffer.limit(sourceBuffer.position() + buffer.remaining());
            buffer.put(sourceBuffer);
        } else {
            //Must copy one type at a time
            int count = source.getCount();
            int size = source.getType().size * source.getComponentType().size;
            byte[] d = new byte[size];
            int pos = sourceBuffer.position();
            int byteStride = view.getByteStride();
            for (int i = 0; i < count; i++) {
                sourceBuffer.get(d);
                buffer.put(d);
                pos += byteStride;
                sourceBuffer.position(pos);
            }
            
        }
        sourceBuffer.limit(limit);
    }
    
    /**
     * Loads data from the uri into this buffer, must call {@link #createBuffer()} to create buffer before
     * loading data into this buffer
     * 
     * @param glTF
     * @param uri
     * @throws IllegalArgumentException If buffer has not bee created
     */
    public void load(GLTF glTF, String uri) throws IOException, URISyntaxException {
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer storage has not bee created, must call createBuffer()");
        }
        ClassLoader loader = getClass().getClassLoader();
        InputStream is = loader.getResourceAsStream(glTF.getPath(uri));
        SimpleLogger.d(getClass(),
                "Loading into buffer with size " + buffer.capacity() + " from " + glTF.getPath(uri));
        int total = load(is);
        is.close();
        if (total != byteLength) {
            SimpleLogger.d(getClass(), "Loaded " + total + " bytes into buffer with capacity " + byteLength);
        }
    }

    /**
     * Loads data from the inputstream into this buffer - at position 0
     * 
     * @param is
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public int load(InputStream is) throws IOException, URISyntaxException {
        ReadableByteChannel byteChannel = Channels.newChannel(is);
        buffer.rewind();
        int read = 0;
        int total = 0;
        while ((read = byteChannel.read(buffer)) > 0) {
            total += read;
        }
        byteChannel.close();
        return total;
    }

    @Override
    public String toString() {
        return toString(0, MAX_BUFFER_PRINT);
    }

    public String toString(int position, int length) {
        String str = "URI: " + uri + ", name: " + getName() + ", byteLength: " + byteLength + (bufferName > 0 ? 
                " VBO " + bufferName : " no VBO");
        
        str += "\n" + BufferUtils.getContentAsString(position, length, buffer);
        return str;
    }

}
