package com.nucleus.mmi;

import com.nucleus.scene.Node;
import com.nucleus.ui.Button;
import com.nucleus.ui.Toggle;

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
