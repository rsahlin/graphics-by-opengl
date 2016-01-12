package com.nucleus.actor;

import com.nucleus.CoreApp;
import com.nucleus.scene.Node;

/**
 * For nodes that contain actor objects
 * To automatically process actor set the {@link LogicProcessor} in {@link CoreApp#setLogicProcessor(LogicProcessor)}
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
     * Returns the actor objects for this node, or null if not set.
     * 
     * @return The actor objects for the node, or null if not set.
     */
    public abstract ActorContainer[] getActorContainer();

    @Override
    public State getControllerState() {
        return controllerState;
    }

}
