package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.renderer.NucleusRenderer.Layer;

/**
 * A Node representing a layer, this node and all children are considrered to belong to the same layer
 * 
 * @author Richard Sahlin
 *
 */
public class LayerNode extends Node {

    @SerializedName("layer")
    private Layer layer;

    @Override
    public LayerNode createInstance() {
        LayerNode copy = new LayerNode();
        return copy;
    }

    @Override
    public LayerNode copy() {
        LayerNode copy = createInstance();
        copy.set(this);
        return copy;
    }

    /**
     * Copies the contents of the source node into this node
     * 
     * @param source
     */
    public void set(LayerNode source) {
        super.set(source);
        this.layer = source.layer;
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

}
