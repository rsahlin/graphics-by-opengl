package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayDeque;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.MeshFactory;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node.MeshType;
import com.nucleus.shader.ShaderProgram;

/**
 * The default node factory implementation
 * 
 * @author Richard Sahlin
 *
 */
public class DefaultNodeFactory implements NodeFactory {

    protected static final String NOT_IMPLEMENTED = "Not implemented: ";
    protected static final String ILLEGAL_NODE_TYPE = "Unknown node type: ";
    protected ArrayDeque<LayerNode> viewStack = new ArrayDeque<LayerNode>(NucleusRenderer.MIN_STACKELEMENTS);

    @Override
    public Node create(NucleusRenderer renderer, MeshFactory meshFactory, Node source,
            RootNode root) throws NodeException {
        if (source.getType() == null) {
            throw new NodeException("Type not set in source node - was it created programatically?");
        }
        NodeType type = null;
        try {
            type = NodeType.valueOf(source.getType());
        } catch (IllegalArgumentException e) {
            // This means the node type is not known.
            throw new IllegalArgumentException(ILLEGAL_NODE_TYPE + source.getType());
        }
        Node copy = internalCreateNode(renderer, root, source, meshFactory);
        return copy;
    }

    @Override
    public void createChildNodes(NucleusRenderer renderer, MeshFactory meshFactory, Node source, Node parent)
            throws NodeException {
        // Recursively create children
        for (Node nd : source.children) {
            Node child = createNode(renderer, meshFactory, nd, parent);
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
     * @param source The node source,
     * @param parent The parent node
     * @return The created node, this will be a new instance of the source node ready to be rendered/processed
     */
    protected Node createNode(NucleusRenderer renderer, MeshFactory meshFactory, Node source,
            Node parent) throws NodeException {
        long start = System.currentTimeMillis();
        Node created = create(renderer, meshFactory, source, parent.getRootNode());
        FrameSampler.getInstance().logTag(FrameSampler.CREATE_NODE + " " + source.getId(), start,
                System.currentTimeMillis());
        boolean isViewNode = false;
        if (NodeType.layernode.name().equals(created.getType())) {
            viewStack.push((LayerNode) created);
            isViewNode = true;
        }
        // created.setRootNode(parent.getRootNode());
        setViewFrustum(source, created);
        createChildNodes(renderer, meshFactory, source, created);
        if (isViewNode) {
            viewStack.pop();
        }
        created.onCreated();
        return created;

    }

    /**
     * Checks if the source node has viewfrustum, if it has it is set in the node.
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

    /**
     * Internal method to create node
     * 
     * @param renderer
     * @param source
     * @param meshFactory
     * @throws NodeException If there is an error creating the node
     * @return Copy of the source node that will be prepared for usage
     */
    protected Node internalCreateNode(NucleusRenderer renderer, RootNode root, Node source, MeshFactory meshFactory)
            throws NodeException {
        try {
            Node node = source.createInstance(root);
            node.create();
            // Copy properties from source node into the created node.
            node.setProperties(source);
            node.copyTransform(source);
            Mesh mesh = meshFactory.createMesh(renderer, node);
            if (mesh != null) {
                node.addMesh(mesh, MeshType.MAIN);
            }
            return node;
        } catch (IOException e) {
            throw new NodeException(e);
        }
    }

    @Override
    public Node create(NucleusRenderer renderer, ShaderProgram program, Mesh.Builder builder, Type<Node> nodeType,
            RootNode root)
            throws NodeException {
        try {
            Node node = Node.createInstance(nodeType, root);
            // TODO Fix generics so that cast is not needed
            Mesh mesh = builder.create();
            if (mesh != null) {
                node.addMesh(mesh, MeshType.MAIN);
            }
            return node;
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            throw new NodeException(e);
        }
    }

}
