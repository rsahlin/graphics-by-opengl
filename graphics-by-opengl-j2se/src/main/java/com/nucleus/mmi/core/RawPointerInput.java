package com.nucleus.mmi.core;

import com.nucleus.mmi.Pointer.PointerAction;
import com.nucleus.mmi.Pointer.Type;

/**
 * A low level raw pointer (touch, mouse or similar) based input event.
 * This is the low level event interface that shall handle only the basic MMI functionality such as
 * pointer down, pointer up, pointer move.
 * This is used in the implementation layer of a platform, for instance to pass on Android touch events, or JOGL mouse
 * events to a unified listener.
 * Gestures and processing of pointer movement shall not be in this interface.
 * 
 * Used by the {@link CoreInput} to create MMI based motion events
 * 
 * @author Richard Sahlin
 *
 */
public interface RawPointerInput {

    /**
     * Index into position array where x position is
     */
    public final static int X = 0;
    /**
     * Index into position array where y position is.
     */
    public final static int Y = 1;

    /**
     * 
     * @param action DOWN, UP or MOVE
     * @param type What type of event, stylus, mouse, finger or mouse
     * @param timestamp The event timestamp
     * @param pointer Pointer index, 0 and updwards.
     * @param position Pointer x,y position, normally in screen coordinates - implementations MUST create new array for
     * each call.
     * @param pressure Touch pressure
     */
    public void pointerEvent(PointerAction action, Type type, long timestamp, int pointer,
            float[] position, float pressure);

}
