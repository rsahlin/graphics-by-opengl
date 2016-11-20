package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.vecmath.Transform;

/**
 * A Node representing a layer with a view
 * Each layer node has its own view matrix, this makes it possible to set the viewpoint separately in the different
 * nodes.
 * One ViewNode could be the scene, and the next the UI - the view in the scene is updated but the UI is still
 * Please not that there is no view stack when rendering, once a view is set it is valid until another node sets the
 * view.
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
        if (source.view != null) {
            view = new Transform(source.view);
        } else {
            view = null;
        }
    }

    @Override
    public void onCreated() {
        super.onCreated();
        if (view == null) {
            throw new IllegalArgumentException("ViewNode shall define view.");
        }
        viewController = new ViewController(view);
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
     * Returns the view transform, this is the same view as in the {@link #viewController}
     * 
     * @return The view transform, or null if not set
     */
    public Transform getView() {
        return view;
    }

    /**
     * Returns the viewcontroller for this node, shall not be null.
     * 
     * @return
     */
    public ViewController getViewController() {
        return viewController;
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
