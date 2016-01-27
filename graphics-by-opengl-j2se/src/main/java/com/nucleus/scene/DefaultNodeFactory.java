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

    private static final String NOT_IMPLEMENTED = "Not implemented";

    @Override
    public Node create(NucleusRenderer renderer, Node source, String reference, RootNode scene) throws IOException {
        try {
            NodeType type = NodeType.valueOf(source.getType());
            Node created = null;
            switch (type) {
            case node:
                created = new Node(source);
                break;
            default:
                throw new IllegalArgumentException(NOT_IMPLEMENTED + type);
            }
            return created;

        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
