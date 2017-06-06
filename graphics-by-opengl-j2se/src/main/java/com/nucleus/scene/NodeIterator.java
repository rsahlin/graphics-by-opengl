package com.nucleus.scene;

import java.util.Iterator;

/**
 * Traverses the nodetree in a breadth first manner.
 *
 */
public class NodeIterator implements Iterator<Node> {

    private final Node start;


    /**
     * Creates a new nodeiterator
     * 
     * @param start
     * @throws IllegalArgumentException If start is null
     */
    public NodeIterator(Node start) {
        if (start == null) {
            throw new IllegalArgumentException("Start node may not be null");
        }
        this.start = start;
    }

    @Override
    public boolean hasNext() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Node next() {
        // TODO Auto-generated method stub
        return null;
    }

}
