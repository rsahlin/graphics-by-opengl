package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.renderer.NucleusRenderer.Layer;

/**
 * A Node representing a layer with a view, the view can be transformed using a {@link #getViewController()}
 * Each layer node has its own view matrix, this makes it possible to set the viewpoint separately in the different
 * nodes.
 * One ViewNode could be the scene, and the next the UI
 * 
 * @author Richard Sahlin
 *
 */
public class ViewNode extends Node {

    /**
     * This can be used to find nodes based on layer
     */
    @SerializedName("layer")
    private Layer layer;

    transient ViewController viewController;

    @Override
    public ViewNode createInstance() {
        ViewNode copy = new ViewNode();
        return copy;
    }

    @Override
    public ViewNode copy() {
        ViewNode copy = createInstance();
        copy.set(this);
        return copy;
    }

    @Override
    public void set(Node source) {
        set((ViewNode) source);
    }

    private void set(ViewNode source) {
        super.set(source);
        this.layer = source.layer;
    }

    @Override
    public void onCreated() {
        super.onCreated();
        viewController = new ViewController(transform);
    }

    /**
     * Returns the layer this node is for
     * 
     * @return
     */
    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }


    /**
     * Returns the viewcontroller for this node, shall not be null.
     * 
     * @return
     */
    public ViewController getViewController() {
        return viewController;
    }


}
