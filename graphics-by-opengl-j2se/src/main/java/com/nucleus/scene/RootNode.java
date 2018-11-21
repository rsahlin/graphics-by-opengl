package com.nucleus.scene;

import java.util.ArrayList;
import java.util.List;

import com.nucleus.assets.AssetManager;
import com.nucleus.exporter.Reference;
import com.nucleus.mmi.NodeInputListener;
import com.nucleus.renderer.NucleusRenderer;

/**
 * Starting point of a nodetree, the root has a collection of nodes the each represent a scene.
 * There shall only be one rootnode at any given time, the root node defines the possible resource that may be
 * needed for the tree.
 * A root node shall be self contained, reference textures and large data sets.
 * All childnodes added must have the same RootNode reference
 * 
 */
public interface RootNode extends Reference {

    /**
     * Creates a new instance of this root, then copies the data from this node into the copy and returns it.
     * This is a shallow copy, it does not copy children.
     * Use this for instance when root is loaded
     * 
     * @param source Source root to copy
     * @return New copy of this node, transient values and children will not be copied.
     * @throws IllegalArgumentException If root is null
     */
    public RootNode createInstance();

    /**
     * Sets the root scene node, this rootnode shall be rootnode of all added children.
     * This is the same as adding the scene by calling {@link #addChild(Node)} on each of the children.
     * 
     * @param node
     * @throws IllegalArgumentException If a node has already been set with a call to this method, or rootnode is not
     * set in scene
     */
    public void setScene(List<Node> scene);

    /**
     * Adds node to current list of currently rendered nodes
     * 
     * @param node
     */
    public void addRenderedNode(Node node);

    /**
     * Swaps the list of rendernodes - the current nodelist will be the visible nodelist and the current nodelist is
     * cleared.
     * Call this method when buffers are swapped so that next call to {@link #getVisibleNodeList()} will return list of
     * visible nodes
     */
    public void swapNodeList();

    /**
     * Returns a copy of visible nodes, this is the list of nodes that have been rendered and is currently visible.
     * 
     * @param visibleList List of visible nodes written here
     * @id Last id returned by calling this method, use this to avoid copying of contents have not changed.
     * @return Id of returned list
     */
    public int getVisibleNodeList(ArrayList<Node> visibleList, int id);

    /**
     * Registers a node as child node (somewhere) on the root node.
     * After calling this method the ID can be used to locate the node
     * NOTE - this shall not be called directly by clients - its called from the {@link Node#addChild(Node)} method
     * 
     * @param child
     * @throws IllegalArgumentException If a node with the same ID is already added to the nodetree
     */
    public void registerChild(Node child);

    /**
     * Unregisters the child from list of nodes within the rootnode
     * NOTE - this shall not be called directly
     * 
     * @param child
     * @param parent
     * @throws IllegalArgumentException If the child is not registered with the rootnode
     */
    public void unregisterChild(Node child);

    /**
     * Release all nodes in the nodetree and destroy the root
     * Call this when you will not use any of the nodes in the tree.
     * This will not free any textures or programs - to do that, use the {@link AssetManager}
     * 
     * @param renderer
     */
    public void destroy(NucleusRenderer renderer);

    /**
     * Returns the {@link NodeInputListener}, or null if not set.
     * This method should normally not be called
     * 
     * @return The {@link NodeInputListener} for this node or null if not set
     */
    public NodeInputListener getObjectInputListener();

    /**
     * Sets the {@link NodeInputListener} for this rootnode, the listener will get callbacks for Nodes that
     * have bounds specified, when there is touch interaction.
     * Remember to set bounds for Nodes that shall get callbacks.
     * 
     * @param objectInputListener Listener to get callback when input interaction is registered on a Node with bounds.
     */
    public void setObjectInputListener(NodeInputListener objectInputListener);

    /**
     * Returns node with matching id, searching through children recursively.
     * 
     * @param id Id of node to return
     * @param type
     * @return First instance of node with matching id, or null if none found
     */
    public <T extends Node> T getNodeById(String id, Class<T> type);

    /**
     * Returns node with matching id, searching through children recursively.
     * Children will be searched by calling {@link #getChildren()} excluding nodes that are switched off.
     * 
     * @param name Name of the type
     * @param type
     * @return First instance of node with matching id, or null if none found
     */
    public <T extends Node> T getNodeByType(String name, Class<T> type);

    /**
     * Returns a list with the childnodes in this root
     * 
     * @return
     */
    public ArrayList<Node> getChildren();

    /**
     * Returns the property for the key, if the key has no value then defaultValue is returned.
     * 
     * @param key
     * @param defaultValue The value to return if key is not set, may be null.
     * @return The property value for key, or defaultValue if not set.
     */
    public String getProperty(String key, String defaultValue);

    public void addChild(Node child);

}
