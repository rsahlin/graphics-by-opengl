package com.nucleus.mmi;

import java.util.ArrayList;
import java.util.List;

import com.nucleus.mmi.Pointer.PointerAction;
import com.nucleus.mmi.Pointer.Type;

/**
 * The low level pointer motion data for one pointer.
 * Holds each pointer position for a pointer motion, beginning with down, each new position and up.
 *
 */
public class PointerMotion {

    /**
     * Each pointer data, the first will be the touch down, followed by movement.
     */
    ArrayList<Pointer> pointerMovement = new ArrayList<>();
    /**
     * Two dimensional min-max values for touch delta:
     * [xMin, xMax, yMin, yMax]
     * Updated when {@link #add(Pointer)} is called.
     */
    private float[] minMax = new float[4];

    public PointerMotion() {
    }

    /**
     * Adds the pointer data to list.
     * This shall only be used by the framework - do NOT call this method.
     * TODO How to hide the method so that it is only visible for PointerInputProcessor
     * 
     * @param data Pointer data
     */
    public void add(Pointer data) {
        pointerMovement.add(data);
        if (getCount() > 1) {
            float[] down = getFirstPosition();
            float[] current = getCurrentPosition();
            minMax[0] = Math.min(current[0] - down[0], minMax[0]);
            minMax[1] = Math.max(current[0] - down[0], minMax[1]);
            minMax[2] = Math.min(current[1] - down[1], minMax[2]);
            minMax[3] = Math.max(current[1] - down[1], minMax[3]);
        }
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
        Pointer last = pointerMovement.get(size - 1);
        if (count >= size) {
            count = size - 1;
        }
        Pointer first = pointerMovement.get((size - 1) - count);
        delta[0] = last.data[0] - first.data[0];
        delta[1] = last.data[1] - first.data[1];
        return delta;
    }

    /**
     * Returns the position where the pointer action started, ie the first pressed position in this action.
     * NOTE! This is a reference to the values. Do not modify!
     * 
     * @return The first position in the touch action, ie the first active position.
     */
    public float[] getFirstPosition() {
        return pointerMovement.get(0).data;
    }

    /**
     * Returns the latest position
     * NOTE! This is a reference to the values. Do not modify!
     * 
     * @return
     */
    public float[] getCurrentPosition() {
        if (pointerMovement.size() == 0) {
            return null;
        }
        return pointerMovement.get(pointerMovement.size() - 1).data;
    }

    /**
     * Returns the n'th pointer data, or null if no data
     * 
     * @param n
     * @return
     */
    public Pointer get(int n) {
        if (pointerMovement.size() > n) {
            return pointerMovement.get(n);
        }
        return null;
    }

    /**
     * Returns the latest pointer data.
     * NOTE! This is a reference to the values. Do not modify!
     * 
     * @return The last received (newest) pointer data
     */
    public Pointer getCurrent() {
        return pointerMovement.get(pointerMovement.size() - 1);
    }

    /**
     * Returns the first (oldest) pointer data, this is the first pointer down action.
     * NOTE! This is a reference to the values. Do not modify!
     * 
     * @return The originating pointer down action.
     */
    public Pointer getFirst() {
        return pointerMovement.get(0);
    }

    /**
     * Creates pointer data for the specified parameter
     * 
     * @param action Pointer event action, eg DOWN, UP
     * @param type The type that the event originates from
     * @param timestamp Time of original low level event
     * @param pointer Pointer number, 0 and up
     * @param data Position of pointer
     * @param pressure
     * @return
     */
    public Pointer create(PointerAction action, Type type, long timestamp, int pointer, float[] data,
            float pressure) {
        return new Pointer(action, type, timestamp, pointer, data, pressure);
    }

    /**
     * Creates pointer data for the specified parameter
     * 
     * @param action Pointer event action, eg DOWN, UP
     * @param type The type that the event originates from
     * @param timestamp Time of original low level event
     * @param pointer Pointer number, 0 and up
     * @param x x value
     * @param y y value
     * @param pressure
     * @return
     */
    public Pointer create(PointerAction action, Type type, long timestamp, int pointer, float x, float y,
            float pressure) {
        return new Pointer(action, type, timestamp, pointer, x, y, pressure);
    }

    /**
     * Returns a list with all pointer data for the current movement.
     * DO NOT MODIFY THESE VALUES
     * 
     * @return
     */
    public List<Pointer> getPointers() {
        return pointerMovement;
    }

    /**
     * Returns the number of pointer data, ie number of touch inputs
     * 
     * @return
     */
    public int getCount() {
        return pointerMovement.size();
    }

    /**
     * Returns reference to array with 4 values, min and max for x and y:
     * [xmin,ymin,xmax,ymax]
     * 
     * @return
     */
    public float[] getMinMax() {
        return minMax;
    }

}
