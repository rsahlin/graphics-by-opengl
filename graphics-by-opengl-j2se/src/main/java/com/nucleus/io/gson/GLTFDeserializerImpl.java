package com.nucleus.io.gson;

import com.google.gson.GsonBuilder;
import com.nucleus.scene.gltf.Node;

/**
 * Deserializer for gltf based Nodes
 *
 */
public class GLTFDeserializerImpl extends AbstractNucleusDeserializerImpl<Node> implements NucleusDeserializer<Node> {

    @Override
    public void registerTypeAdapter(GsonBuilder builder) {
        // Dont register any type adapters
    }

}
