package com.nucleus.scene;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Implementation of RootNode - used to get unified support when loading/instantiating scenes
 *
 */
public class RootNodeImpl extends AbstractRootNode implements RootNode {

    public static final String GLTF_PATH = "glTFPath";

    @SerializedName(CHILDREN)
    private ArrayList<Node> childNodes = new ArrayList<>();

    /**
     * DO NOT USE
     */
    public RootNodeImpl() {
        super();
    }

    @Override
    public void setScene(List<Node> scene) {
        if (scene == null) {
            throw new IllegalArgumentException("Scene is null");
        }
        for (Node node : scene) {
            // addChild(node);
        }
    }

    @Override
    public <T extends Node> T getNodeById(String id, Class<T> type) {
        T result;
        for (Node n : getChildren()) {
            if ((result = n.getNodeById(id, type)) != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public <T extends Node> T getNodeByType(String name, Class<T> type) {
        T result;
        for (Node n : getChildren()) {
            if ((result = n.getNodeByType(name, type)) != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public ArrayList<Node> getChildren() {
        return childNodes;
    }

    @Override
    public void addChild(Node child) {
        childNodes.add(child);
    }

    @Override
    public RootNode createInstance() {
        RootNodeImpl copy = new RootNodeImpl();
        copy.copy(this);
        return copy;
    }

}
