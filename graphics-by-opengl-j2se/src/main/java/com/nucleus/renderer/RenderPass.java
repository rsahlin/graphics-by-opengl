package com.nucleus.renderer;

import com.nucleus.io.BaseReference;

/**
 * Definition of a renderpass
 *
 */
public class RenderPass extends BaseReference {
    
    /**
     * Defines the result output
     */
    private RenderTarget target;
    /**
     * Render state for the target
     */
    private RenderState renderState;

    /**
     * Returns the render target
     * @return
     */
    public RenderTarget getTarget() {
        return target;
    }

    /**
     * Returns the renderstate for the target
     * @return
     */
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
