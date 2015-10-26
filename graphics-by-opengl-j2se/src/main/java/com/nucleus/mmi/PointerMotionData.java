package com.nucleus.mmi;

import java.util.ArrayList;

import com.nucleus.mmi.PointerData.PointerAction;

/**
 * The pointer motion data for one pointer.
 * Holds each pointer position for a pointer motion, beginning with down, each new position and up.
 * 
 * @author Richard Sahlin
 *
 */
public class PointerMotionData {

    /**
     * Each pointer data, the first will be the touch down, followed by movement.
     */
    ArrayList<PointerData> pointerMovement = new ArrayList<>();

    /**
     * Adds the pointer data to list.
     * 
     * @param data Pointer data
     */
    void add(PointerData data) {
        pointerMovement.add(data);

    }

    /**
     * Returns the touch state of the pointer data in this class.
     * Returns true if there is pointer down action, possibly followed by pointer move, but no pointer up action.
     * Returns false if there is no pointer down or no pointer move OR the last pointer is pointer up.
     * 
     * @return True if the pointer data contained is touch down, optionally followed by pointer move but the last action
     * is NOT pointer up.
     */
    public boolean isDown() {
        int size = pointerMovement.size();
        if (size == 0 || pointerMovement.get(size - 1).action != PointerAction.UP) {
            return true;
        }
        return false;
    }

    /**
     * Returns the delta values for x and y between the latest value and count before.
     * 
     * @param count
     * @return Array with x and y delta between last pointer data counting back count number, or null if only
     * one position is added (ie only the down position)
     */
    public float[] getDelta(int count) {
        int size = pointerMovement.size();
        if (size <= 1) {
            return null;
        }
        float[] delta = new float[2];
        PointerData last = pointerMovement.get(size - 1);
        if (count >= size) {
            count = size - 1;
        }
        PointerData first = pointerMovement.get((size - 1) - count);
        delta[0] = last.position[0] - first.position[0];
        delta[1] = last.position[1] - first.position[1];
        return delta;
    }

    /**
     * Returns the position where the pointer action started, ie the first pressed position in this action.
     * NOTE! This is a reference to the values. Do not modify!
     * 
     * @return The first position in the touch action, ie the first active position.
     */
    public float[] getFirstPosition() {
        return pointerMovement.get(0).position;
    }

    /**
     * Returns the latest position
     * NOTE! This is a reference to the values. Do not modify!
     * 
     * @return
     */
    public float[] getCurrentPosition() {
        return pointerMovement.get(pointerMovement.size() - 1).position;
    }

    /**
     * Returns the latest pointer data.
     * NOTE! This is a reference to the values. Do not modify!
     * 
     * @return The last received (newest) pointer data
     */
    public PointerData getCurrent() {
        return pointerMovement.get(pointerMovement.size() - 1);
    }

    /**
     * Returns the first (oldest) pointer data, this is the first pointer down action.
     * NOTE! This is a reference to the values. Do not modify!
     * 
     * @return The originating pointer down action.
     */
    public PointerData getFirst() {
        return pointerMovement.get(0);
    }

}