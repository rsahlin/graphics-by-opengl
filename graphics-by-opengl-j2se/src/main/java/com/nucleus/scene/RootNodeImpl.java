package com.nucleus.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.ManagedList;
import com.nucleus.mmi.ObjectInputListener;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderPass;
import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.Target;

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
public class RootNodeImpl implements RootNode {

    public static class Builder {

        ViewFrustum viewFrustum;

        public Builder() {
        }

        public RootNode create(String id) throws NodeException {
            RootNodeImpl root = new RootNodeImpl();
            // setRoot(root);
            RenderPass pass = new RenderPass("RenderPass", new RenderTarget(Target.FRAMEBUFFER, null),
                    new RenderState(),
                    Pass.MAIN);
            /*
             * 
             * Node created = super.create("rootnode");
             * if (created instanceof RenderableNode<?>) {
             * ViewFrustum vf = new ViewFrustum();
             * vf.setOrthoProjection(-0.8889f, 0.8889f, -0.5f, 0.5f, 0, 10);
             * ((RenderableNode<?>) created).setViewFrustum(vf);
             * created.setPass(Pass.ALL);
             * ArrayList<RenderPass> rp = new ArrayList<>();
             * rp.add(pass);
             * ((RenderableNode<?>) created).setRenderPass(rp);
             * }
             * created.onCreated();
             * root.addChild(created);
             * return root;
             */
            return root;
        }

    }

    public static final String PROPERTIES = "properties";
    public static final String CHILDREN = "children";

    public static final String GLTF_PATH = "glTFPath";
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
    private Map<String, String> properties;

    @SerializedName(CHILDREN)
    private ArrayList<Node> childNodes = new ArrayList<>();

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

    /**
     * DO NOT USE
     */
    public RootNodeImpl() {
        super();
    }

    private void copy(RootNodeImpl source) {
        setProperties(source);
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
    public void setScene(List<Node> scene) {
        if (scene == null) {
            throw new IllegalArgumentException("Scene is null");
        }
        for (Node node : scene) {
            // addChild(node);
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
    public ObjectInputListener getObjectInputListener() {
        return objectInputListener;
    }

    @Override
    public void setObjectInputListener(ObjectInputListener objectInputListener) {
        this.objectInputListener = objectInputListener;
    }

    @Override
    public <T extends Node> T getNodeById(String id, Class<T> type) {
        T result;
        for (Node n : getChildren()) {
            if ((result = n.getNodeById(id, type)) != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public <T extends Node> T getNodeByType(String name, Class<T> type) {
        T result;
        for (Node n : getChildren()) {
            if ((result = n.getNodeByType(name, type)) != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public ArrayList<Node> getChildren() {
        return childNodes;
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

    @Override
    public void addChild(Node child) {
        childNodes.add(child);
    }

    @Override
    public RootNode createInstance() {
        RootNodeImpl copy = new RootNodeImpl();
        copy.copy(this);
        return copy;
    }

}
