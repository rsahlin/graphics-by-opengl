package com.nucleus.scene;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.common.ManagedList;
import com.nucleus.mmi.ObjectInputListener;
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
public abstract class RootNode extends AbstractNode {

    private static final String GLTF_PATH = "glTFPath";
    private static final String GLTF_URIS = "glTFUris";

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

    @SerializedName(GLTF_PATH)
    private String glTFPath;
    @SerializedName(GLTF_URIS)
    private String[] glTFUris;

    /**
     * When a node is rendered it is added to this list, this is for the current frame - will change as nodes are
     * rendered
     */
    transient private ArrayList<Node> renderNodeList = new ArrayList<>();
    /**
     * List of last displayed frame rendered nodes - this is the currently visible nodes.
     */
    transient private ManagedList<Node> visibleNodeList = new ManagedList<>();
    /**
     * Set this to get callbacks on MMI events for this node, handled by {@link NodeInputListener}
     */
    transient protected ObjectInputListener objectInputListener;

    /**
     * Table with all added childnodes and their id.
     */
    transient private Hashtable<String, Node> childNodeTable = new Hashtable<>();

    protected RootNode() {
        super();
        setType(NodeTypes.rootnode);
    }

    public void copyTo(RootNode target) {
        target.set(this);
    }

    public void set(RootNode source) {
        super.set(source);
        glTFPath = source.glTFPath;
        glTFUris = source.glTFUris;
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
     * Adds node to current list of currently rendered nodes
     * 
     * @param node
     */
    public void addRenderedNode(Node node) {
        renderNodeList.add(node);
    }

    /**
     * Swaps the list of rendernodes - the current nodelist will be the visible nodelist and the current nodelist is
     * cleared.
     * Call this method when buffers are swapped so that next call to {@link #getVisibleNodeList()} will return list of
     * visible nodes
     */
    public void swapNodeList() {
        visibleNodeList.updateList(renderNodeList);
        renderNodeList.clear();
    }

    /**
     * Returns a copy of visible nodes, this is the list of nodes that have been rendered and is currently visible.
     * 
     * @param visibleList List of visible nodes written here
     * @id Last id returned by calling this method, use this to avoid copying of contents have not changed.
     * @return Id of returned list
     */
    public int getVisibleNodeList(ArrayList<Node> visibleList, int id) {
        return visibleNodeList.getList(visibleList, id);
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

    /**
     * Returns the {@link ObjectInputListener}, or null if not set.
     * This method should normally not be called, it is handled by {@link NodeInputListener}
     * 
     * @return The {@link ObjectInputListener} for this node or null if not set
     */
    protected ObjectInputListener getObjectInputListener() {
        return objectInputListener;
    }

    /**
     * Sets the {@link ObjectInputListener} for this rootnode, the listener will get callbacks for Nodes that
     * have bounds specified, when there is touch interaction.
     * Remember to set bounds for Nodes that shall get callbacks.
     * 
     * @param objectInputListener Listener to get callback when input interaction is registered on a Node with bounds.
     */
    public void setObjectInputListener(ObjectInputListener objectInputListener) {
        this.objectInputListener = objectInputListener;
    }

    /**
     * Returns the index of the glTF uri
     * 
     * @param uri
     * @return The index of the glTF asset or -1 if not found
     */
    public int getGLTFIndex(String uri) {
        if (glTFUris == null) {
            return -1;
        }
        for (int i = 0; i < glTFUris.length; i++) {
            if (uri.contentEquals(glTFUris[i])) {
                return i;
            }
        }
        return -1;
    }

    public String getGLTFPath() {
        return glTFPath;
    }

}
