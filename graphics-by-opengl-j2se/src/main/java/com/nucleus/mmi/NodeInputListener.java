package com.nucleus.mmi;

import com.nucleus.scene.Node;
import com.nucleus.ui.Toggle;

/**
 * Listener for (high level) input events on a (visible) object
 * This is mostly intended for ui elements
 *
 */
public interface NodeInputListener extends InputListener<Node> {

    /**
     * Called when a state change is detected on a Toggle
     * 
     * @param toggle
     */
    public void onStateChange(Toggle toggle);

}
