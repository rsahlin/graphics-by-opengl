package com.nucleus.scene;

import com.nucleus.properties.EventManager.EventHandler;
import com.nucleus.properties.Property;

/**
 * Handles node related actions, there shall be only one node controller for each nodetree.
 * TODO How to check that only one nodecontroller is created for a RootNode
 * 
 * @author Richard Sahlin
 *
 */
public class NodeController implements EventHandler {

    /**
     * The key to register in the property handler for this class
     */
    private final static String HANDLER_KEY = "node";

    public enum Actions {
        /**
         * This will switch the specified node
         * Split value to get nodeId, switchId
         */
        SWITCH();
    }

    private RootNode root;

    /**
     * Creates a new node controller with the specified root node.
     * 
     * @param root
     */
    public NodeController(RootNode root) {
        this.root = root;
    }

    @Override
    public void handleEvent(String key, String value) {
        Property p = Property.create(value);
    }

    @Override
    public void handleObjectEvent(Object obj, String key, String value) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getHandlerCategory() {
        return HANDLER_KEY;
    }

}
