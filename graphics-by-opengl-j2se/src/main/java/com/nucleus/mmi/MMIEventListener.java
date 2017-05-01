package com.nucleus.mmi;

/**
 * Interface for receiving input events on input actions.
 * This is the low level input events that are processed into MMI events.
 * 
 * @author Richard Sahlin
 *
 */
public interface MMIEventListener {

    /**
     * Callback for MMI based input event.
     * 
     * @param event
     */
    public void onInputEvent(MMIPointerEvent event);
}
