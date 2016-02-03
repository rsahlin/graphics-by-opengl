package com.nucleus.scene;

import java.io.IOException;

import com.nucleus.renderer.NucleusRenderer;

/**
 * The default node factory implementation
 * 
 * @author Richard Sahlin
 *
 */
public class DefaultNodeFactory implements NodeFactory {

    protected static final String NOT_IMPLEMENTED = "Not implemented: ";
    protected static final String ILLEGAL_NODE_TYPE = "Unknown node type: ";

    @Override
    public Node create(NucleusRenderer renderer, Node source, RootNode scene) throws IOException {
        NodeType type = null;
        try {
            type = NodeType.valueOf(source.getType());
        } catch (IllegalArgumentException e) {
            // This means the node type is not known.
            throw new IllegalArgumentException(ILLEGAL_NODE_TYPE + source.getType());
        }
        switch (type) {
        case node:
            return new Node(source);
        case layernode:
            return new LayerNode((LayerNode) source);
        default:
            throw new IllegalArgumentException(NOT_IMPLEMENTED + type);
        }
    }

}
