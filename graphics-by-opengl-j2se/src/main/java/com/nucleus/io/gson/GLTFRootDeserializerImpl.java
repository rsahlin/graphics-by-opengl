package com.nucleus.io.gson;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.nucleus.scene.gltf.GLTFRootNode;
import com.nucleus.scene.gltf.Node;

public class GLTFRootDeserializerImpl extends NucleusDeserializerImpl<Node> implements NucleusRootDeserializer<Node> {

    @Override
    public void registerTypeAdapter(GsonBuilder builder) {
        // Dont register any type adapters
    }

    @Override
    public Type getRootNodeTypeClass() {
        return GLTFRootNode.class;
    }

}
