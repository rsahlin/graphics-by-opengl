package com.nucleus.scene;

/**
 * The state of a node.
 * This can be used to skip nodes from being rendered or processed.
 * 
 * @author Richard Sahlin
 *
 */
public enum NodeState {

    /**
     * Node is on, rendered and actors processed
     */
    on(),
    /**
     * Node is off, not rendered and no actors processed
     */
    off(),
    /**
     * Node is rendered, but no actors processed
     */
    render(),
    /**
     * Node is not rendered, but actors processed
     */
    actor();

}
