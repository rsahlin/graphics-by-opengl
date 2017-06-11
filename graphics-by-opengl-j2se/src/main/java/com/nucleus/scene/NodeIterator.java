package com.nucleus.scene;

import java.util.Iterator;

/**
 * Superclass for node iterators
 *
 */
public abstract class NodeIterator implements Iterator<Node> {

    protected final Node start;

    /**
     * Creates a new node iterator, for the nodetree starting with node
     * 
     * @param start
     * @throws IllegalArgumentException If start is null
     */
    protected NodeIterator(Node start) {
        if (start == null) {
            throw new IllegalArgumentException("Start node may not be null");
        }
        this.start = start;
    }

}
