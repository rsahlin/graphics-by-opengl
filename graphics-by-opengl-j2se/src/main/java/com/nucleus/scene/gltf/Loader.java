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

public class Loader {

    public static GLTF loadAsset(String path, String name) throws IOException {
        SimpleLogger.d(Loader.class, "Loading glTF asset:" + path + name);
        ClassLoader loader = Loader.class.getClassLoader();
        InputStream is = loader.getResourceAsStream(path + name);
        return loadAsset(path, is);
    }

    public static GLTF loadAsset(String path, InputStream is) throws IOException {
        try {
            Reader reader = new InputStreamReader(is, "UTF-8");
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            GLTF glTF = gson.fromJson(reader, GLTF.class);
            glTF.setPath(path);
            Loader.loadBuffers(glTF, glTF.getBuffers());
            return glTF;
        } catch (UnsupportedEncodingException e) {
            SimpleLogger.d(Loader.class, e.getMessage());
            return null;
        }
    }

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
