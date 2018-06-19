package com.nucleus.scene.gltf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.annotations.SerializedName;
import com.nucleus.SimpleLogger;

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

public class Buffer {

    private static final String URI = "uri";
    private static final String BYTE_LENGTH = "byteLength";
    private static final String NAME = "name";

    @SerializedName(URI)
    private String uri;
    @SerializedName(BYTE_LENGTH)
    private int byteLength = -1;
    @SerializedName(NAME)
    private String name;

    transient ByteBuffer buffer;

    /**
     * Creates a new buffer with the specified byteLength - the buffer will be created by calling
     * {@link #createBuffer()}
     * 
     * @param byteLength
     */
    public Buffer(int byteLength) {
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
     * 
     * @return
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        buffer = ByteBuffer.allocateDirect(byteLength).order(ByteOrder.nativeOrder());
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
        ClassLoader loader = Loader.class.getClassLoader();
        URL url = loader.getResource(glTF.getPath() + uri);
        SimpleLogger.d(getClass(),
                "Loading into buffer with size " + buffer.capacity() + " from " + glTF.getPath() + uri);
        Path p = Paths.get(url.toURI());
        ByteChannel bc = java.nio.file.Files.newByteChannel(p);
        buffer.rewind();
        int read = 0;
        int total = 0;
        while ((read = bc.read(buffer)) > 0) {
            total += read;
        }
        bc.close();
        if (total != byteLength) {
            SimpleLogger.d(getClass(), "Loaded " + total + " bytes into buffer with capacity " + byteLength);
        }
    }

}
