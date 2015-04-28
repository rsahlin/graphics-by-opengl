package com.nucleus.mmi;

import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

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
    private Vector2D zoom;
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
    public MMIPointerEvent(PointerMotionData pointer1, PointerMotionData pointer2, Vector2D delta1, Vector2D delta2,
            float dot1, float dot2) {
        action = Action.ZOOM;
        pointerData = pointer1;
        zoom = new Vector2D();
        float value = (dot1 + dot2) / 2;
        zoom.vector[VecMath.X] = value;
        zoom.vector[VecMath.Y] = value;
        zoom.vector[Vector2D.MAGNITUDE] = -(delta1.vector[Vector2D.MAGNITUDE] + delta2.vector[Vector2D.MAGNITUDE]);
    }

    /**
     * Returns the zoom value - only valid if action is ZOOM.
     * 
     * @return Zoom value, or null if action is not ZOOM
     */
    public Vector2D getZoom() {
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

}
