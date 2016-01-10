package com.nucleus.logic;

import com.nucleus.CoreApp;
import com.nucleus.scene.Node;

/**
 * For nodes that contain logic objects
 * To automatically process logic set the {@link LogicProcessor} in {@link CoreApp#setLogicProcessor(LogicProcessor)}
 * 
 * @author Richard Sahlin
 *
 */
public abstract class ActorNode extends Node implements ActorController {

    transient public State controllerState = State.CREATED;

    /**
     * Default constructor
     */
    protected ActorNode() {
        super();
    }

    protected ActorNode(ActorNode source) {
        super(source);
    }

    /**
     * Returns the logic objects for this node, or null if not set.
     * 
     * @return The logic objects for the node, or null if not set.
     */
    public abstract ActorContainer[] getLogicContainer();

    @Override
    public State getControllerState() {
        return controllerState;
    }

}
