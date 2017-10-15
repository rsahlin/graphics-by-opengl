package com.nucleus.renderer;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.BaseReference;

/**
 * Definition of a renderpass
 *
 */
public class RenderPass extends BaseReference {
    
    public static final String TARGET = "target";
    public static final String RENDERSTATE = "renderState";
    public static final String PASS = "pass";
    
    /**
     * Defines the result output
     */
    @SerializedName(TARGET)
    private RenderTarget target;
    /**
     * Render state for the target
     */
    @SerializedName(RENDERSTATE)
    private RenderState renderState;

    /**
     * The pass that this object defines data for
     */
    @SerializedName(PASS)
    private Pass pass;
    
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
    
    public void setPass(Pass pass) {
        this.pass = pass;
    }
    
    public Pass getPass() {
        return pass;
    }
    
    
}
