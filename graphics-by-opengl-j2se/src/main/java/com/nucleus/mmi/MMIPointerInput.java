package com.nucleus.mmi;

/**
 * Interface for receiving input events on input actions.
 * This is the low level input events that are processed into MMI events.
 * 
 * @author Richard Sahlin
 *
 */
public interface MMIPointerInput {

    /**
     * Callback for MMI based touch/mouse input event.
     * 
     * @param event
     */
    public void onInput(MMIPointer event);

}
