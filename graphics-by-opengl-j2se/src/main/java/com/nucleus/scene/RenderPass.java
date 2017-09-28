package com.nucleus.scene;

import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderTarget;

/**
 * Node that represent a render pass
 *
 */
public class RenderPass extends Node {

    private RenderTarget target;
    private RenderState renderState;

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    @Deprecated
    protected RenderPass() {
        super();
    }

    @Override
    public RenderPass createInstance(RootNode root) {
        if (getId() == null) {
            throw new IllegalArgumentException("RenderPass must have id");
        }
        RenderPass copy = new RenderPass(root);
        copy.set(this);
        return copy;
    }

    /**
     * Copies the data from the source renderpass
     * @param source
     */
    public void set(RenderPass source) {
        super.set(source);
        this.target = source.target;
        this.renderState = source.renderState;
    }
    
    /**
     * @param root
     */
    public RenderPass(RootNode root) {
        super(root, NodeTypes.renderpass);
    }

    public RenderTarget getTarget() {
        return target;
    }

    public RenderState getRenderState() {
        return renderState;
    }
    
    public void setTarget(RenderTarget renderTarget) {
        this.target = renderTarget;
    }
    
    public void setRenderState(RenderState renderState) {
        this.renderState = renderState;
    }

}
