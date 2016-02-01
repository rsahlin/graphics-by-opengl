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

    /**
     * Creates a new instance of a layer node.
     * 
     * @throws IllegalArgumentException If layer is null
     */
    public LayerNode(Layer layer) {
        if (layer == null) {
            throw new IllegalArgumentException();
        }
        this.layer = layer;
    }

    /**
     * Creates a new instance copy of the source node
     * 
     * @param source
     */
    public LayerNode(LayerNode source) {
        set(source);
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

}
