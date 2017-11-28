package com.nucleus.scene;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.nucleus.assets.AssetManager;
import com.nucleus.renderer.NucleusRenderer;

/**
 * Starting point of a nodetree, the root has a collection of nodes the each represent a scene.
 * There shall only be one rootnode at any given time, the root node defines the possible resource that may be
 * needed for the tree.
 * A root node shall be self contained, reference textures and large data sets.
 * This class can be serialized using GSON
 * All childnodes added must have the same RootNode reference
 * 
 * @author Richard Sahlin
 *
 */
public abstract class RootNode extends Node {

    /**
     * Default id for the root node
     */
    public static final String ROOTNODE_ID = "rootnode";

    /**
     * Pre defined ids that can be used for scenes and make it more convenient find a scene.
     * 
     */
    public enum Scenes {
        credit(),
        select(),
        settings(),
        game(),
        about(),
    }

    /**
     * Set to true when node is added or removed
     */
    transient private boolean updated = false;
    transient private Hashtable<NodeIterator, ArrayList<Node>> iterators = new Hashtable<NodeIterator, ArrayList<Node>>();
    /**
     * When a node is rendered it is added to this list, this is for the current frame - will change as nodes are
     * rendered
     */
    transient private ArrayList<Node> renderNodeList = new ArrayList<>();
    /**
     * List of last displayed frame rendered nodes - this is the currently visible nodes.
     */
    transient private ArrayList<Node> visibleNodeList = new ArrayList<>();

    /**
     * Table with all added childnodes and their id.
     */
    transient private Hashtable<String, Node> childNodeTable = new Hashtable<>();

    protected RootNode() {
        super();
        setType(NodeTypes.rootnode);
    }

    /**
     * Sets the root scene node, this rootnode shall be rootnode of all added children.
     * This is the same as adding the scene by calling {@link #addChild(Node)} on each of the children.
     * 
     * @param node
     * @throws IllegalArgumentException If a node has already been set with a call to this method, or rootnode is not
     * set in scene
     */
    public void setScene(List<Node> scene) {
        if (scene == null) {
            throw new IllegalArgumentException("Scene is null");
        }
        for (Node node : scene) {
            addChild(node);
        }
    }

    /**
     * Creates a new instance of RootNode, implement in RootNode subclasses to return the implementation instance.
     * 
     * @return A new instance of RootNode implementation
     */
    public abstract RootNode createInstance();

    /**
     * Call this when a node is added or removed to the nodetree.
     */
    public void updated() {
        updated = true;
    }

    /**
     * Returns a list with all nodes, starting with the scene node, listed using the specified node iterator.
     * If scene has been updated, node added or removed, then the nodelist is recreated, otherwise the same list is
     * returned.
     * 
     * @param iterator
     * @return List with nodes listed by the iterator - do NOT modify this list, it will be returned in subsequent calls
     * unless the scene
     * tree has been altered (node added or removed)
     */
    public ArrayList<Node> getNodes(NodeIterator iterator) {
        ArrayList<Node> result = iterators.get(iterator);
        if (result == null || updated) {
            result = createNodeList(iterator, result);
        }
        return result;
    }

    private ArrayList<Node> createNodeList(NodeIterator iterator, ArrayList<Node> result) {
        if (result == null) {
            result = new ArrayList<>();
            iterators.put(iterator, result);
        }
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    /**
     * Adds node to current list of currently rendered nodes
     * 
     * @param node
     */
    public void addRenderedNode(Node node) {
        renderNodeList.add(node);
    }

    /**
     * Returns a list with previous frames rendered nodes.
     * 
     * @return
     */
    public ArrayList<Node> getRenderedNodes() {
        return visibleNodeList;
    }

    /**
     * Swaps the list of rendernodes - the current nodelist will be the visible nodelist and the current nodelist is
     * cleared.
     * Call this method when buffers are swapped so that next call to {@link #getVisibleNodeList()} will return list of
     * visible nodes
     */
    public void swapNodeList() {
        visibleNodeList = renderNodeList;
        renderNodeList = null;
        renderNodeList = new ArrayList<>();
    }

    /**
     * Returns list of visible nodes, this is the list of nodes that have been rendered and is currently visible.
     * DO NOT MODIFY THIS LIST
     * 
     * @return
     */
    public ArrayList<Node> getVisibleNodeList() {
        return visibleNodeList;
    }

    /**
     * Registers a node as child node (somewhere) on the root node.
     * After calling this method the ID can be used to locate the node
     * NOTE - this shall not be called directly by clients - its called from the {@link Node#addChild(Node)} method
     * 
     * @param child
     * @throws IllegalArgumentException If a node with the same ID is already added to the nodetree
     */
    @Override
    protected void registerChild(Node child) {
        if (childNodeTable.containsKey(child.getId())) {
            throw new IllegalArgumentException("Already added child with id:" + child.getId());
        }
        childNodeTable.put(child.getId(), child);
    }

    /**
     * Unregisters the child from list of nodes within the rootnode
     * NOTE - this shall not be called directly
     * 
     * @param child
     * @param parent
     * @throws IllegalArgumentException If the child is not registered with the rootnode
     */
    @Override
    protected void unregisterChild(Node child) {
        if (childNodeTable.remove(child.getId()) == null) {
            throw new IllegalArgumentException("Node not registered with root:" + child.getId());
        }
    }

    /**
     * Release all nodes in the nodetree and destroy the root
     * Call this when you will not use any of the nodes in the tree.
     * This will not free any textures or programs - to do that, use the {@link AssetManager}
     * 
     * @param renderer
     */
    @Override
    public void destroy(NucleusRenderer renderer) {
        for (Node node : childNodeTable.values()) {
            node.destroy(renderer);
        }
        renderNodeList = null;
        visibleNodeList = null;
        childNodeTable = null;
    }

}
