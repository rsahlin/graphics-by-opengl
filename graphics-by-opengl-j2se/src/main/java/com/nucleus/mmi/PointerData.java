package com.nucleus.mmi;

/**
 * Data for a pointer input
 * 
 * @author Richard Sahlin
 *
 */
public class PointerData {

    /**
     * The different pointer actions
     *
     */
    public enum PointerAction {
        /**
         * Pointer down action, this means that the pointer is in a 'pressed' state.
         * If the following action is MOVE it shall be regarded as a pressed motion event, ie touch move or
         * mouse button pressed move.
         */
        DOWN(0),
        /**
         * Pointer up action, this means that the pointer is in an 'not-pressed' state.
         * If the following action is MOVE it shall be regarded as move without press, ie hover move or mouse move
         * (without button pressed)
         */
        UP(1),
        /**
         * Pointer move action, keep track of the UP/DOWN action to know if this is a pressed move (eg touch move).
         */
        MOVE(2),
        /**
         * Zoom action from the input device - note that not all input devices can support this.
         */
        ZOOM(3);

        private int action;

        private PointerAction(int action) {
            this.action = action;
        }

    }

    /**
     * The index for the first pointer
     */
    public final static int POINTER_1 = 0;
    /**
     * The index for the second pointer
     */
    public final static int POINTER_2 = 1;
    /**
     * The index for the third pointer
     */
    public final static int POINTER_3 = 2;
    /**
     * The index for the fourth pointer
     */
    public final static int POINTER_4 = 3;
    /**
     * The index for the fifth pointer
     */
    public final static int POINTER_5 = 4;
    /**
     * The current pointer position
     */
    public final float[] position;
    /**
     * Pointer index, 0 and up
     */
    public int pointer;

    public long timeStamp;

    /**
     * The pointer action, ie what the type of input action, DOWN, MOVE or UP
     */
    public PointerAction action;

    /**
     * Creates a new pointerdata with pointer index and x,y pos
     * 
     * @param action The pointer action, DOWN, MOVE or UP
     * @param timestamp The time of the event, in milliseconds.
     * @param pointer Pointer index, eg the touch finger index.
     * @param position Array with x and y position.
     */
    public PointerData(PointerAction action, long timestamp, int pointer, float[] position) {
        this.action = action;
        this.timeStamp = timestamp;
        this.pointer = pointer;
        this.position = new float[] { position[PointerListener.X], position[PointerListener.Y] };
    }

}
