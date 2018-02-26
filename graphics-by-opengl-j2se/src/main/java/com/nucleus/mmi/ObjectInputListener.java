package com.nucleus.mmi;

/**
 * Listener for (high level) input events on a (visible) object
 * This is mostly intended for ui elements
 *
 */
public interface ObjectInputListener {

    /**
     * Called when the object has a pointer action performed on it, check PointerData for action.
     * 
     * @param event The pointer data for the event
     * @return True if the object consumes the pointer event
     */
    public boolean onInputEvent(PointerData event);

    /**
     * Called when a pointer down has been recorded and pointer has been dragged.
     * This may be interpreted as a drag, rectangle select or a swipe by the client.
     * 
     * @param drag The pointer events making up the drag motion
     * @return True if the object consumes the action
     */
    public boolean onDrag(PointerMotionData drag);

}
