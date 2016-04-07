package com.nucleus.scene;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

/**
 * Node that can switch between lists of different child-nodes, this type of switch node will return a specified
 * list of child nodes based on the {@link #switchChildren(String)} method called.
 * Ie, it can choose one of several paths.
 * The node itself can be rendered as any normal Node
 * This node can be serialized using GSON
 */
public class SwitchNode extends Node {

    /**
     * The current and starting switch, set this to the name of the first key to be active.
     */
    @SerializedName("switch")
    private String nodeSwitch;
    /**
     * List of current children
     */
    private ArrayList<Node> currentChildren;
    /**
     * Mapping of key to list of child nodes to return when {@link #getChildren()} is called.
     */
    @SerializedName("childmap")
    private HashMap<String, ArrayList<Node>> childMap;

    @Override
    public ArrayList<Node> getChildren() {
        if (currentChildren == null && nodeSwitch != null) {
            registerChildren(nodeSwitch, currentChildren);
        }
        return currentChildren;
    }

    /**
     * Switches the children to the specified key, this means that the children registered with key will be returned
     * when {@link #getChildren()} is called.
     * 
     * @param key
     */
    protected void switchChildren(String key) {
        currentChildren = childMap.get(key);
        nodeSwitch = key;
    }

    /**
     * Stores a list of child nodes with a specified key, to enable the provided children call
     * {@link #switchChildren(String)} with the key.
     * 
     * @param key
     * @param children
     */
    public void registerChildren(String key, ArrayList<Node> children) {
        childMap.put(key, children);
    }

}
