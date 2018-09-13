package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayDeque;

import com.nucleus.geometry.MeshBuilder;
import com.nucleus.opengl.GLException;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.AbstractNode.NodeTypes;

/**
 * The default node factory implementation - use this to create instances of loaded nodes
 * Will create one mesh for the Node by calling MeshFactory
 * TODO - cleanup this and {@link Node.Builder}
 * 
 * @author Richard Sahlin
 *
 */
public class DefaultNodeFactory implements NodeFactory {

    protected static final String NOT_IMPLEMENTED = "Not implemented: ";
    protected static final String ILLEGAL_NODE_TYPE = "Unknown node type: ";
    protected ArrayDeque<LayerNode> viewStack = new ArrayDeque<LayerNode>(NucleusRenderer.MIN_STACKELEMENTS);
    /**
     * Number of meshes to create by calling MeshFactory or Builder
     */
    protected int meshCount = 1;

    @Override
    public Node create(NucleusRenderer renderer, Node source, RootNode root) throws NodeException {
        if (source.getType() == null) {
            throw new NodeException("Type not set in source node - was it created programatically?");
        }
        Node copy = internalCreateNode(renderer, root, source);
        return copy;
    }

    @Override
    public void createChildNodes(NucleusRenderer renderer, Node source, Node parent)
            throws NodeException {
        // Recursively create children if there are any
        if (source.getChildren() == null) {
            return;
        }
        for (Node nd : source.getChildren()) {
            createNode(renderer, nd, parent);
        }
    }

    /**
     * Creates a new node from the source node, creating child nodes as well, looking up resources as needed.
     * The parent node will be set as parent to the created node
     * Before returning the node #onCreated() will be called
     * 
     * @param source The node source,
     * @param parent The parent node
     * @return The created node, this will be a new instance of the source node ready to be rendered/processed
     * @throws IllegalArgumentException If node could not be added to parent
     */
    protected Node createNode(NucleusRenderer renderer, Node source,
            Node parent) throws NodeException {
        long start = System.currentTimeMillis();
        Node created = create(renderer, source, parent.getRootNode());
        parent.addChild(created);
        FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_NODE, " " + source.getId(), start,
                System.currentTimeMillis());
        boolean isViewNode = false;
        if (NodeTypes.layernode.name().equals(created.getType())) {
            viewStack.push((LayerNode) created);
            isViewNode = true;
        }
        // Call #onCreated() on the created node before handling children - parent needs to be fully created before
        // the children.
        created.onCreated();
        createChildNodes(renderer, source, created);
        if (isViewNode) {
            viewStack.pop();
        }
        return created;
    }

    /**
     * Internal method to create node
     * 
     * @param renderer
     * @param source
     * @throws NodeException If there is an error creating the node
     * @return Copy of the source node that will be prepared for usage
     */
    protected Node internalCreateNode(NucleusRenderer renderer, RootNode root, Node source)
            throws NodeException {
        try {
            Node node = source.createInstance(root);
            // Make sure same class instance as source - if not then new Node introduced without proper
            // createInstance() method
            if (!node.getClass().getSimpleName().contentEquals(source.getClass().getSimpleName())) {
                throw new IllegalArgumentException("Class is not same, forgot to implement createInstance()? source: "
                        + source.getClass().getSimpleName() + ", instance: " + node.getClass().getSimpleName());
            }
            if (node instanceof RenderableNode<?>) {
                RenderableNode<?> rNode = (RenderableNode<?>) node;
                for (int i = 0; i < meshCount; i++) {
                    MeshBuilder meshBuilder = rNode.createMeshBuilder(renderer, null);
                    if (meshBuilder != null) {
                        meshBuilder.create(rNode);
                        if (rNode.getBounds() == null) {
                        	rNode.setBounds(meshBuilder.createBounds());
                        }
                    }
                }
            }
            node.create();
            return node;
        } catch (IOException | GLException e) {
            throw new NodeException(e);
        }
    }

    /**
     * Sets the number of meshes to create by calling MeshFactory in create methods.
     * Default is 1.
     * 
     * @param meshCount Number of meshes to create when Node is created.
     */
    public void setMeshCount(int meshCount) {
        this.meshCount = meshCount;
    }

}
