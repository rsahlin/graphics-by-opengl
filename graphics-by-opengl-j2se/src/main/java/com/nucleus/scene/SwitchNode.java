package com.nucleus.scene;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * Node that can switch between one active child node, this is done by returning this when {@link #getChildren()} is
 * called.
 * Switching the active node is done by calling
 * The node itself can be rendered as any normal Node
 * This node can be serialized using GSON
 */
public class SwitchNode extends Node {

    /**
     * The Id of the current active child Node, if null all child nodes will be returned when {@link #getChildren()}
     * is called.
     */
    @SerializedName("active")
    private String active;

    /**
     * The active children, this is the list that will be returned when {@link #getChildren()} is called
     */
    transient private ArrayList<Node> activeChildren;

    @Override
    public SwitchNode createInstance() {
        return new SwitchNode();
    }

    @Override
    public SwitchNode copy() {
        SwitchNode copy = createInstance();
        copy.set(this);
        return copy;
    }

    /**
     * Sets the values from the source to this node, this will not set transient values.
     * 
     * @param source
     */
    public void set(SwitchNode source) {
        super.set(source);
        this.active = source.active;
    }

    @Override
    public ArrayList<Node> getChildren() {
        if (active == null) {
            return super.getChildren();
        }
        if (activeChildren == null) {
            setActive(active);
        }
        return activeChildren;
    }

    /**
     * Sets the active node in the children.
     * Next call to {@link #getChildren()} will return a list containing the node with the matching id.
     * 
     * @param activeId Id of the child node to set as active, all other children will be inactive
     */
    protected void setActive(String activeId) {
        active = activeId;
        if (activeChildren == null) {
            activeChildren = new ArrayList<>();
        } else {
            activeChildren.clear();
        }
        Node active = getChildById(activeId);
        if (active != null) {
            activeChildren.add(active);
        }
    }

}
