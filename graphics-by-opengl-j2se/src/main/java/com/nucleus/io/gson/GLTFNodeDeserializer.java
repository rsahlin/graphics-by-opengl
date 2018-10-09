package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.nucleus.scene.GLTFRootNode;
import com.nucleus.scene.gltf.Node;

public class GLTFNodeDeserializer extends NucleusRootDeserializer<Node> {

    @Override
    public void registerTypeAdapter(GsonBuilder builder) {
        // Dont register any type adapters
    }

    @Override
    public Type getRootNodyTypeClass() {
        return GLTFRootNode.class;
    }

}
