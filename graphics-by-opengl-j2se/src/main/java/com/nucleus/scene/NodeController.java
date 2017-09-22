package com.nucleus.scene;

import com.nucleus.SimpleLogger;
import com.nucleus.event.EventManager;
import com.nucleus.event.EventManager.EventHandler;
import com.nucleus.properties.Property;

/**
 * Handles node related actions, there shall be only one node controller for each nodetree.
 * Will be called by {@link Node} when there is a pointer input on a node with bounds and {@link Property} defined with
 * {@value #HANDLER_KEY}
 * TODO How to check that only one nodecontroller is created for a RootNode
 * 
 * @author Richard Sahlin
 *
 */
public class NodeController implements EventHandler<Node> {

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
    public void handleEvent(Node node, String key, String value) {
        Actions action = Actions.valueOf(key);
        Property p = Property.create(value);
        switch (action) {
        case SWITCH:
            NavigationController.getInstance().setActiveSwitch(root, p.getKey(), p.getValue());
            break;
        default:
            SimpleLogger.d(getClass(), "Not handled event action: " + action + " with value: " + value);
        }
    }

    /**
     * Registers this class as a eventhandler for the key, if key is null the {@link #HANDLER_KEY} is used.
     * NOTE! Only register ONE instance. This shall be called with the
     * {@link #handleObjectEvent(Object, String, String)} method which will resolve the needed target.
     * TODO How to make sure only one instance of this class is registered?
     * 
     * @param key The key to register this controller for, or null to use default.
     */
    @Override
    public void registerEventHandler(String key) {
        EventManager.getInstance().register(key, this);
    }

    @Override
    public String getHandlerCategory() {
        return HANDLER_KEY;
    }

}
