package com.nucleus.mmi;

import com.nucleus.mmi.PointerData.PointerAction;

/**
 * A pointer (touch, mouse or similar) based input event.
 * This is the low level event interface that shall handle only the basic MMI functionallity such as
 * pointer down, pointer up, pointer move.
 * Gestures and processing of pointer movement shall not be in this interface.
 * 
 * @author Richard Sahlin
 *
 */
public interface PointerListener {

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
     * @param timestamp The event timestamp
     * @param pointer Pointer index, 0 and updwards.
     * @param position Pointer x,y position, normally in screen coordinates - implementations MUST create new array for
     * each call.
     */
    public void pointerEvent(PointerAction action, long timestamp, int pointer, float[] position);

}