package com.nucleus.ui;

import com.nucleus.scene.Node;

/**
 * Listener for (high level) input events on a (visible) UI element in a set of nodes.
 * This is mostly intended for ui elements when one listener is used for all nodes.
 *
 */
public interface UIElementInput extends UIInput<Node> {

    /**
     * Called when a state change is detected on a Toggle
     * 
     * @param toggle
     */
    public void onStateChange(Toggle toggle);

    /**
     * Called when a press is detected on a Button
     * 
     * @param button
     */
    public void onPressed(Button button);

}
