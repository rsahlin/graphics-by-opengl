package com.nucleus.scene;

import com.nucleus.common.Type;

public class RenderToTargetNode extends Node {

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    @Deprecated
    protected RenderToTargetNode() {
    }

    protected RenderToTargetNode(RootNode root, Type<Node> type) {
        super(root, type);
    }

    private RenderToTargetNode(RootNode root) {
        super(root, NodeTypes.rendertotarget);
    }

    @Override
    public RenderToTargetNode createInstance(RootNode root) {
        RenderToTargetNode copy = new RenderToTargetNode(root);
        copy.set(this);
        return copy;
    }

}
