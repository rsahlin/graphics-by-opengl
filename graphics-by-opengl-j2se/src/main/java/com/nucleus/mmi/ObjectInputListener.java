package com.nucleus.mmi;

/**
 * Listener for (high level) input events on a (visible) object
 * This is mostly intended for ui elements
 *
 */
public interface ObjectInputListener {

    /**
     * Called when the object has a click action performed on it
     * 
     * @param The pointer data for the click
     * @return True if the object consumes the click action
     */
    public boolean onClick(PointerData click);

    /**
     * Called when a pointer down has been recorded and pointer has been dragged.
     * This may be interpreted as a drag, rectangle select or a swipe by the client.
     * 
     * @param drag The pointer events making up the drag motion
     * @return True if the object consumes the action
     */
    public boolean onDrag(PointerMotionData drag);


}
