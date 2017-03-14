package com.nucleus.scene;

import com.nucleus.event.EventManager;
import com.nucleus.event.EventManager.EventHandler;
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

    /**
     * Registers this class as a eventhandler for the key, if key is null the {@link #HANDLER_KEY} is used.
     * NOTE! Only register ONE instance. This shall be called with the
     * {@link #handleObjectEvent(Object, String, String)} method which will resolve the needed target.
     * TODO How to make sure only one instance of this class is registered?
     * 
     * @param key The key to register this controller for, or null to use default.
     */
    public void registerEventHandler(String key) {
        EventManager.getInstance().register(key, this);
    }

    @Override
    public void handleObjectEvent(Object obj, String category, String value) {
        Actions action = Actions.valueOf(category);
        Property p = Property.create(value);
        switch (action) {
        case SWITCH:
            SwitchNode node = (SwitchNode) root.getScene().getNodeById(p.getKey());
            if (node != null) {
                node.setActive(p.getValue());
            }
            break;
        }
    }

    @Override
    public String getHandlerCategory() {
        return HANDLER_KEY;
    }

}
