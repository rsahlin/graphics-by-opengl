package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.mmi.ObjectInputListener;

/**
 * Handles interception of input events on a node, builds on {@link MMIPointerEvent}
 * This is intended for Nodes that need to implement custom logic for touch input, by setting the
 * {@link ObjectInputListener}
 * on the node.
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
     * @param nodes List of nodes to check - this must be in draw order, ie first drawn node will be first.
     * Iterate through this from end to beginning
     * @param event
     * @return True if a node has consumed the input event event
     */
    public boolean onInputEvent(ArrayList<Node> nodes, MMIPointerEvent event);

}
