package com.nucleus.scene;

import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderTarget;

/**
 * Node that represent a render pass
 *
 */
public class RenderPass extends Node {

	private RenderTarget target = new RenderTarget();
	private RenderState renderState = new RenderState();
	
    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    @Deprecated
	protected RenderPass() {
		super();
	}

    @Override
    public RenderPass createInstance(RootNode root) {
    	RenderPass copy = new RenderPass(root);
        copy.set(this);
        return copy;
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
	
}
