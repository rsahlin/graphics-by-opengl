package com.nucleus.mmi;

import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vec2;

public class MMIPointerEvent {

    public enum Action {
        /**
         * A pointer becomes active, ie pressed. For touch input devices this means a touch down.
         */
        ACTIVE(0),
        /**
         * A pointer becomes inactive, ie not pressed. For touch input devices this means touch up.
         */
        INACTIVE(1),
        /**
         * Move input event, this means that the user has pressed and moved.
         * For touch input devices this is a touch move.
         */
        MOVE(2),
        /**
         * More than one pointer is active, moved closer to zoom in and moved away to zoom out.
         */
        ZOOM(3);

        private int action;

        private Action(int action) {
            this.action = action;
        }
    }

    private Action action;
    private PointerMotionData pointerData;
    private Vec2 zoom;
    private int finger;

    public MMIPointerEvent(Action action, int finger, PointerMotionData pointerData) {
        this.action = action;
        this.pointerData = pointerData;
        this.finger = finger;
    }

    /**
     * Creates a new mmi pointer event, the action is ZOOM.
     * 
     * @param pointer1
     * @param pointer2
     * @param delta1
     * @param delta2
     * @param dot1
     * @param dot2
     */
    public MMIPointerEvent(PointerMotionData pointer1, PointerMotionData pointer2, Vec2 delta1, Vec2 delta2,
            float dot1, float dot2) {
        action = Action.ZOOM;
        pointerData = pointer1;
        zoom = new Vec2();
        float value = (dot1 + dot2) / 2;
        zoom.vector[VecMath.X] = value;
        zoom.vector[VecMath.Y] = value;
        zoom.vector[Vec2.MAGNITUDE] = (delta1.vector[Vec2.MAGNITUDE] + delta2.vector[Vec2.MAGNITUDE]);
    }

    public void setZoom(float x, float y) {
        zoom = new Vec2();
        float magnitude = (float) Math.sqrt(x * x + y * y);
        zoom.vector[Vec2.MAGNITUDE] = magnitude;
        zoom.vector[VecMath.X] = x / magnitude;
        zoom.vector[VecMath.Y] = y / magnitude;
    }

    /**
     * Returns the zoom value - only valid if action is ZOOM.
     * 
     * @return Zoom value, or null if action is not ZOOM
     */
    public Vec2 getZoom() {
        return zoom;
    }

    /**
     * Returns the pointer data
     * 
     * @return
     */
    public PointerMotionData getPointerData() {
        return pointerData;
    }

    /**
     * Returns the action for this event, use this for an easy way to detect simple MMI actions.
     * 
     * @return The MMI user input action.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the finger number for this event
     * 
     * @return Finger number, 0 and up
     */
    public int getFinger() {
        return finger;
    }

}
