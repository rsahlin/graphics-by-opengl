package com.nucleus.scene.gltf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nucleus.SimpleLogger;
import com.nucleus.assets.AssetManager;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;

/**
 * GLTF asset loader.
 * Use {@link AssetManager#loadGLTFAsset(String, String, int)} to load assets that are part of a scene or shall be
 * handled in a uniform manner.
 * 
 *
 */
public class Loader {

    /**
     * Loads a glTF asset, loading buffers and binary references as needed.
     * The returned asset is resolved using {@link RuntimeResolver} and is ready to be used.
     * 
     * @param gles
     * @param path
     * @param is
     * @return
     * @throws IOException
     * @throws GLTFException If there is an error in the glTF or it cannot be resolved
     */
    public static GLTF loadAsset(String path, InputStream is)
            throws IOException, GLTFException {
        try {
            Reader reader = new InputStreamReader(is, "UTF-8");
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            GLTF glTF = gson.fromJson(reader, GLTF.class);
            glTF.setPath(path);
            glTF.resolve();
            Loader.loadBuffers(glTF, glTF.getBuffers());
            return glTF;
        } catch (UnsupportedEncodingException e) {
            SimpleLogger.d(Loader.class, e.getMessage());
            return null;
        }
    }

    /**
     * Loads the buffers
     * 
     * @param glTF
     * @param buffers
     * @throws IOException
     */
    protected static void loadBuffers(GLTF glTF, Buffer[] buffers) throws IOException {
        try {
            for (Buffer b : buffers) {
                b.createBuffer();
                b.load(glTF, b.getUri());
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

}
