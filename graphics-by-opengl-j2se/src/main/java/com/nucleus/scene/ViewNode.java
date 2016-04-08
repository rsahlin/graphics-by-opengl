package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.vecmath.Transform;

/**
 * A Node representing a layer with a view
 * Each layer node has its own view matrix, this makes it possible to set the viewpoint separately in the different
 * nodes.
 * One ViewNode could be the scene, and the next the UI - the view in the scene is updated but the UI is still
 * Please not that there is no view stack, once a view is set it is valid until another node sets the view.
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

    /**
     * The current view location, this is translated into the view transform that is set into
     * the renderer.
     * Use this to move the location of the view.
     * TODO Remove view from rootnode, make it possible to find parent ViewNode from children (at any level)
     */
    @SerializedName("view")
    private Transform view;

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

    /**
     * Copies the contents of the source node into this node
     * 
     * @param source
     */
    public void set(ViewNode source) {
        super.set(source);
        this.layer = source.layer;
        if (source.view != null) {
            view = new Transform(source.view);
        } else {
            view = null;
        }
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
     * Returns the view transform
     * 
     * @return The view transform, or null if not set
     */
    public Transform getView() {
        return view;
    }

    /**
     * Sets the view transform for this rootnode, this is normally done by defining it in the scene.
     * 
     * @param view
     */
    public void setView(Transform view) {
        this.view = view;
    }

}
