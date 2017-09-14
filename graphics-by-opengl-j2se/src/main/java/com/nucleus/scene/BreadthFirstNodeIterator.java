package com.nucleus.scene;

import java.util.ArrayList;

/**
 * Traverses the nodetree in a breadth first manner.
 *
 */
public class BreadthFirstNodeIterator extends NodeIterator {

    ArrayList<Node> children;

    /**
     * Creates a new nodeiterator
     * 
     * @param start
     * @throws IllegalArgumentException If start is null
     */
    public BreadthFirstNodeIterator(Node start) {
        super(start);
    }

    @Override
    public boolean hasNext() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Node next() {
        if (children == null) {
            children = start.getChildren();
        }
        
        return null;
    }

    /**
     * Returns the node and childnodes as breadth first list
     * 
     * @param start
     * @return
     */
    public ArrayList<Node> breadthFirstList(Node start) {
        return addNode(null, null, start);
    }


    private ArrayList<Node> addNode(ArrayList<Node> result, ArrayList<Node> children, Node node) {
        if (result == null) {
            result = new ArrayList<>();
        }
        if (children == null) {
            children = new ArrayList<>();
        }
        result.add(node);
        children.addAll(node.getChildren());
        addChildren(result, children, node);
        return result;
    }

    private void addChildren(ArrayList<Node> result, ArrayList<Node> parents, Node parent) {
        ArrayList<Node> children = new ArrayList<>();
        for (Node n : parents) {
            addNode(result, children, n);
        }
    }

}
