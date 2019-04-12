package com.nucleus.ui;

import com.nucleus.exporter.Reference;
import com.nucleus.mmi.Pointer;

/**
 * Base class for UI elements, this is the base for focus and touch events to UI.
 * 
 * @author Richard Sahlin
 *
 */
public interface Element extends Reference {

    public enum Type {
        BUTTON(),
        TOGGLE();
    }

    /**
     * Called when the object has a pointer action performed on it, check PointerData for action.
     * 
     * @param obj The object that the input event is recorded on
     * @param event The pointer data for the event
     */
    public void onInputEvent(Pointer event);

    /**
     * Called on the object when a click has been detected
     * 
     * @param event
     * @param listener Optional listener, or null
     */
    public void onClick(Pointer event, UIElementInput listener);

    /**
     * Returns the ui element type of this object - use this to detect what type of Element it is
     * 
     * @return
     */
    public Type getElementType();

}
