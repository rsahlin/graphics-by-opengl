package com.nucleus.mmi;

/**
 * Interface for recieveing input events on input actions.
 * This is low level input events processed into MMI events.
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
    public void inputEvent(MMIPointerEvent event);
}
