package com.nucleus.scene;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.common.Type;
import com.nucleus.scene.gltf.GLTF;

/**
 * Node containing a glTF model
 *
 */
public class GLTFNode extends Node {

    private static final String GLTF_NAME = "glTFName";

    @SerializedName(GLTF_NAME)
    private String glTFName;

    transient private GLTF glTF;

    private GLTFNode(RootNode root, Type<Node> type) {
        super(root, type);
    }

    @Override
    public Node createInstance(RootNode root) {
        GLTFNode copy = new GLTFNode(root, NodeTypes.gltfnode);
        copy.set(this);
        return copy;
    }

    /**
     * Copy values into this node from the source, used when new instance is created
     * 
     * @param source
     */
    public void set(GLTFNode source) {
        super.set(source);
        this.glTFName = source.glTFName;
    }

    public String getGLTFName() {
        return glTFName;
    }

    @Override
    public void onCreated() {
        super.onCreated();
        if (glTFName != null) {
            int index = getRootNode().getGLTFIndex(glTFName);
            try {
                glTF = AssetManager.getInstance().loadGLTFAsset(getRootNode().getGLTFPath(), glTFName, index);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
