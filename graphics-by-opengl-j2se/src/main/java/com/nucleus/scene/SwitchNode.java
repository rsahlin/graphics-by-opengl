package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.gson.PostDeserializable;
import com.nucleus.renderer.NucleusRenderer.NodeRenderer;

/**
 * Node that can switch between one active child node, the active node is set by calling {@link #setActive(String)}
 * This will mark the specified Node as {@link State#ON} the other children will be toggled to {@link State#OFF}
 * This node can be serialized using GSON
 */
public class SwitchNode extends AbstractNode implements PostDeserializable {

    private static final int MAX_CHILD_COUNT = 2;

    /**
     * The Id of the current active child Node, if null all child nodes will be returned when {@link #getChildren()}
     * is called.
     */
    @SerializedName("active")
    private String active;

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    @Deprecated
    protected SwitchNode() {
    }

    private SwitchNode(RootNode root) {
        super(root, NodeTypes.switchnode);
    }

    @Override
    public SwitchNode createInstance(RootNode root) {
        SwitchNode copy = new SwitchNode(root);
        copy.set(this);
        return copy;
    }

    /**
     * Sets the values from the source to this node, this will not set transient values.
     * 
     * @param source
     * @throws ClassCastException If source is not {@link #SwitchNode()}
     */
    @Override
    public void set(AbstractNode source) {
        set((SwitchNode) source);
    }

    private void set(SwitchNode source) {
        super.set(source);
        this.active = source.active;
    }

    /**
     * Sets the active child node, will only alter children in this node
     * Next call to {@link #getChildren()} will return a list containing the node with the matching id.
     * 
     * @param activeId Id of the child node to set as active, all other children will be inactive
     */
    protected void setActive(String activeId) {
        active = activeId;
        for (Node child : children) {
            if (child.getId().equals(activeId)) {
                child.setState(State.ON);
            } else {
                child.setState(State.OFF);
            }
        }
    }

    /**
     * Returns the id of the active child.
     * 
     * @return
     */
    protected String getActive() {
        return active;
    }

    @Override
    public void postDeserialize() {
        if (children.size() > MAX_CHILD_COUNT) {
            throw new IllegalArgumentException("SwitchNode does not allow more than " + MAX_CHILD_COUNT + " children");
        }
        if (active != null) {
            setActive(active);
        }
    }

    @Override
    public void create() {
        // TODO Auto-generated method stub

    }
}
