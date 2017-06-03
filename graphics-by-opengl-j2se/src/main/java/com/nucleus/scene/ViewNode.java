package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.renderer.NucleusRenderer.Layer;

/**
 * A Node representing a layer with a view, the view can be transformed using a {@link #getViewController()}
 * Each layer node has its own view matrix, this makes it possible to set the viewpoint separately in the different
 * nodes.
 * One ViewNode could be the scene, and the next the UI
 * The ViewNode must have transform, when rendering each layer shall re-set the node view tansform making it independent
 * from parent.
 * 
 * @author Richard Sahlin
 *
 */
public class ViewNode extends Node {

    /**
     * This can be used to find nodes based on layer, it can also be used to render based on layer.
     * Each time a layer is defined the Node view transform shall be re-set.
     * This means layer can be used to create object that is separate from transform hierarchy, for instance adding UI
     * elements from within children.
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
        // Make sure ViewNode has transform
        if (transform == null) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " " + getId() + " must have transform");
        }
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
