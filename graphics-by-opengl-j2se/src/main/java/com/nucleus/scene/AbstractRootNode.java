package com.nucleus.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.ManagedList;
import com.nucleus.io.BaseReference;
import com.nucleus.mmi.UIElementInput;
import com.nucleus.renderer.NucleusRenderer;

/**
 * Provides support minimal shared scene data - use this when importing scene data using JSON
 * Adds support for saving visible (rendered) nodes and objectinputlistener.
 *
 */
public abstract class AbstractRootNode extends BaseReference implements RootNode {

    public static final String PROPERTIES = "properties";
    public static final String CHILDREN = "children";

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
     * Properties for this node
     */
    @SerializedName(PROPERTIES)
    protected Map<String, String> properties;

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
     * Set this to get callbacks on MMI events for this node
     */
    transient protected UIElementInput objectInputListener;
    /**
     * Table with all added childnodes and their id.
     */
    transient private Hashtable<String, Node> childNodeTable = new Hashtable<>();

    protected void copy(RootNodeImpl source) {
        setProperties(source);
    }

    @Override
    public UIElementInput getObjectInputListener() {
        return objectInputListener;
    }

    @Override
    public void setObjectInputListener(UIElementInput objectInputListener) {
        this.objectInputListener = objectInputListener;
    }

    /**
     * Copies the properties from the source node to this
     * 
     * @param source
     */
    private void setProperties(RootNodeImpl source) {
        if (source.properties == null || source.properties.size() == 0) {
            return;
        }
        if (properties == null) {
            properties = new HashMap<>();
        }
        for (String key : source.properties.keySet()) {
            properties.put(key, source.properties.get(key));
        }
    }

    @Override
    public void addRenderedNode(Node node) {
        renderNodeList.add(node);
    }

    /**
     * Swaps the list of rendernodes - the current nodelist will be the visible nodelist and the current nodelist is
     * cleared.
     * Call this method when buffers are swapped so that next call to {@link #getVisibleNodeList()} will return list of
     * visible nodes
     */
    @Override
    public void swapNodeList() {
        visibleNodeList.updateList(renderNodeList);
        renderNodeList.clear();
    }

    @Override
    public int getVisibleNodeList(ArrayList<Node> visibleList, int id) {
        return visibleNodeList.getList(visibleList, id);
    }

    @Override
    public void registerChild(Node child) {
        if (childNodeTable.containsKey(child.getId())) {
            throw new IllegalArgumentException("Already added child with id:" + child.getId());
        }
        childNodeTable.put(child.getId(), child);
    }

    @Override
    public void unregisterChild(Node child) {
        if (childNodeTable.remove(child.getId()) == null) {
            throw new IllegalArgumentException("Node not registered with root:" + child.getId());
        }
    }

    @Override
    public void destroy(NucleusRenderer renderer) {
        for (Node node : childNodeTable.values()) {
            node.destroy(renderer);
        }
        renderNodeList = null;
        visibleNodeList = null;
        childNodeTable = null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        if (properties == null) {
            return defaultValue;
        }
        String val = properties.get(key);
        if (val != null) {
            return val;
        }
        return defaultValue;
    }

}
