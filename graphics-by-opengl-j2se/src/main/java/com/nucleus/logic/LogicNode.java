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
public abstract class LogicNode extends Node implements ActorController {

    public State state = State.CREATED;

    /**
     * Returns the logic objects for this node, or null if not set.
     * 
     * @return The logic objects for the node, or null if not set.
     */
    public abstract LogicContainer[] getLogicContainer();

    @Override
    public State getState() {
        return state;
    }

}
