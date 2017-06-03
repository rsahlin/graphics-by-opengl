package com.nucleus.scene;

import java.util.ArrayDeque;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.geometry.MeshFactory;
import com.nucleus.io.ResourcesData;
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
    protected ArrayDeque<ViewNode> viewStack = new ArrayDeque<ViewNode>(NucleusRenderer.MIN_STACKELEMENTS);

    @Override
    public Node create(NucleusRenderer renderer, MeshFactory meshFactory, ResourcesData resources, Node source)
            throws NodeException {
        NodeType type = null;
        try {
            type = NodeType.valueOf(source.getType());
        } catch (IllegalArgumentException e) {
            // This means the node type is not known.
            throw new IllegalArgumentException(ILLEGAL_NODE_TYPE + source.getType());
        }
        try {
            Node copy = (Node) type.getTypeClass().newInstance();
            copy.set(source);
            return copy;
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void createChildNodes(NucleusRenderer renderer, MeshFactory meshFactory, ResourcesData resources,
            Node source, Node parent) throws NodeException {
        // Recursively create children
        for (Node nd : source.children) {
            Node child = createNode(renderer, meshFactory, resources, nd, parent);
            if (child != null) {
                parent.addChild(child);
            }
        }
    }

    /**
     * Creates a new node from the source node, creating child nodes as well, looking up resources as needed.
     * The new node will be returned, it is not added to the parent node - this shall be done by the caller.
     * The new node will have parent as its parent node
     * 
     * @param resources The scene resources
     * @param source The node source,
     * @param parent The parent node
     * @return The created node, this will be a new instance of the source node ready to be rendered/processed
     */
    protected Node createNode(NucleusRenderer renderer, MeshFactory meshFactory, ResourcesData resources, Node source,
            Node parent) throws NodeException {
        Node created = create(renderer, meshFactory, resources, source);
        boolean isViewNode = false;
        if (NodeType.viewnode.name().equals(created.getType())) {
            viewStack.push((ViewNode) created);
            isViewNode = true;
        }
        created.setRootNode(parent.getRootNode());
        setViewFrustum(source, created);
        createChildNodes(renderer, meshFactory, resources, source, created);
        if (isViewNode) {
            viewStack.pop();
        }
        created.onCreated();
        return created;

    }

    /**
     * Checks if the node data has viewfrustum data, if it has it is set in the node.
     * 
     * @param source The source node containing the viewfrustum
     * @param node Node to check, or null
     */
    protected void setViewFrustum(Node source, Node node) {
        if (node == null) {
            return;
        }
        ViewFrustum projection = source.getViewFrustum();
        if (projection == null) {
            return;
        }
        node.setViewFrustum(new ViewFrustum(projection));
    }

}
