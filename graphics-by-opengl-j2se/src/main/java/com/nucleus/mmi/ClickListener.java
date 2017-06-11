package com.nucleus.mmi;

/**
 * Event and listener for click actions on a (visible) object
 * This is mostly intended for ui elements
 *
 */
public interface ClickListener {

    /**
     * Called when the object has a click action performed on it
     * 
     * @return True if the object consumes the click action
     */
    public boolean onClick();

}
