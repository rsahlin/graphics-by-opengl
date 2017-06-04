package com.nucleus.scene;

import com.nucleus.mmi.MMIPointerEvent;

/**
 * Handles interception of input events on a node, builds on {@link MMIPointerEvent}
 *
 */
public interface NodeInputListener {
    /**
     * Set this property to true for nodes that shall check pointer input
     */
    public static final String ONCLICK = "onclick";

    /**
     * Recursively check nodes for the input event, when a node consumes the event true will be returned.
     * 
     * @param node
     * @param event
     * @return True if a node has consumed the input event event
     */
    public boolean onInputEvent(Node node, MMIPointerEvent event);

}
