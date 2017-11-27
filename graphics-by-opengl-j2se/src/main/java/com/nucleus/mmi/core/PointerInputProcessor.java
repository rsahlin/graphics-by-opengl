package com.nucleus.mmi.core;

import java.util.HashSet;
import java.util.Set;

import com.nucleus.SimpleLogger;
import com.nucleus.geometry.Vertex2D;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.mmi.PointerData;
import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.mmi.PointerData.Type;
import com.nucleus.mmi.PointerMotionData;
import com.nucleus.vecmath.Vector2D;

/**
 * Process incoming pointer based events (for instance touch or mouse) and turn into easier to handle MMI actions.
 * 
 * @author Richard Sahlin
 *
 */
public class PointerInputProcessor implements PointerListener {

    public final static int MAX_POINTERS = 5;

    /**
     * Pointer motion data, one for each supported pointer.
     */
    PointerMotionData[] pointerMotionData = new PointerMotionData[MAX_POINTERS];

    /**
     * Scale and offset value for incoming pointer values, this can be used to normalize pointer values.
     * Set to 1/width and 1/height to normalize.
     * Values are: scalex, scaley, translatex, translatey
     */
    private final float[] transform = new float[] { 1, 1, 0, 0 };
    private final float[] scaledPosition = new float[2];

    Set<MMIEventListener> mmiListeners = new HashSet<MMIEventListener>();

    private int pointerCount = 0;
    /**
     * If a movement is less than this then don't count. Use to filter out too small movements.
     */
    private float moveThreshold = 3;

    /**
     * Set to false to disable actions from two pointers, ZOOM
     */
    private boolean processTwoPointers = true;

    /**
     * Default constructor
     */
    public PointerInputProcessor() {
    }

    /**
     * Enable or disable processing of two pointer input - ZOOM.
     * 
     * @param processTwoPointers True to enable two pointer input (ZOOM) false to disable
     */
    public void setProcessTwoPointers(boolean processTwoPointers) {
        this.processTwoPointers = processTwoPointers;
    }

    @Override
    public void pointerEvent(PointerAction action, Type type, long timestamp, int pointer, float[] position) {
        if (pointer >= MAX_POINTERS) {
            return;
        }
        scaledPosition[X] = position[X] * transform[X] + transform[2];
        scaledPosition[Y] = position[Y] * transform[Y] + transform[3];
        switch (action) {
            case MOVE:
                addAndSend(new MMIPointerEvent(com.nucleus.mmi.MMIPointerEvent.Action.MOVE, pointer,
                        pointerMotionData[pointer]),
                        pointerMotionData[pointer].create(action, timestamp, pointer, scaledPosition));
                // More than one pointer is or has been active.
                if (processTwoPointers && pointerCount == 2 && pointer == 1) {
                    processTwoPointers();
                }
                break;
            case DOWN:
                pointerCount = pointer + 1;
                pointerMotionData[pointer] = new PointerMotionData(type);
                addAndSend(new MMIPointerEvent(com.nucleus.mmi.MMIPointerEvent.Action.ACTIVE, pointer,
                        pointerMotionData[pointer]),
                        pointerMotionData[pointer].create(action, timestamp, pointer, scaledPosition));
                break;
            case UP:
                pointerCount--;
                if (pointerCount < 0) {
                    System.out.println("PointerInputProcessor: ERROR: pointerCount= " + pointerCount);
                }
                addAndSend(new MMIPointerEvent(com.nucleus.mmi.MMIPointerEvent.Action.INACTIVE, pointer,
                        pointerMotionData[pointer]),
                        pointerMotionData[pointer].create(action, timestamp, pointer, scaledPosition));
                break;
            case ZOOM:
                if (pointerMotionData[pointer] == null) {
                    pointerMotionData[pointer] = new PointerMotionData(type);
                }
                MMIPointerEvent zoom = new MMIPointerEvent(com.nucleus.mmi.MMIPointerEvent.Action.ZOOM, pointer,
                        pointerMotionData[pointer]);
                zoom.setZoom(position[X] * transform[X], position[Y] * transform[Y]);
                sendToListeners(zoom);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void addAndSend(MMIPointerEvent event, PointerData pointerData) {
        pointerMotionData[pointerData.pointer].add(pointerData);
        sendToListeners(event);
    }

    private void processTwoPointers() {
        PointerMotionData pointer1 = pointerMotionData[PointerData.POINTER_1];
        PointerMotionData pointer2 = pointerMotionData[PointerData.POINTER_2];
        // Find point between the 2 points.
        float[] middle = Vertex2D.middle(pointer1.getFirstPosition(), pointer2.getFirstPosition());
        float[] toMiddle = new float[2];
        // Fetch touch movement as 2D vectors
        Vector2D vector1 = getDeltaAsVector(pointer1, 1);
        Vector2D vector2 = getDeltaAsVector(pointer2, 1);
        // Check if movement from or towards middle.
        if (vector1 != null && vector2 != null) {
            // System.out.println("Twopointer delta1: " + vector1.vector[Vector2D.MAGNITUDE] + " pos: "
            // + pointer1.getCurrentPosition()[0] + ", " + pointer1.getCurrentPosition()[1]);
            // if (vector1.vector[Vector2D.MAGNITUDE] > moveThreshold ||
            // vector2.vector[Vector2D.MAGNITUDE] > moveThreshold) {

            Vertex2D.getDistance(middle, pointer1.getCurrentPosition(), toMiddle);
            Vector2D center1 = new Vector2D(toMiddle);
            Vertex2D.getDistance(middle, pointer2.getCurrentPosition(), toMiddle);
            Vector2D center2 = new Vector2D(toMiddle);

            float angle1 = (float) Math.acos(vector1.dot(center1)) * 57.2957795f;
            float angle2 = (float) Math.acos(vector2.dot(center2)) * 57.2957795f;
            if ((angle1 > 135 && angle2 > 135) || (angle1 < 45 && angle2 < 45)) {
                zoom(pointer1, pointer2, vector1, vector2, center1, center2);
            } else // if (vector1.vector[Vector2D.MAGNITUDE] < moveThreshold) {
                   // If one touch is very small then count the other.
                   // TODO Maybe use magnitude as a factor and weigh angles together
            if ((angle2 > 135) || (angle2 < 45)) {
                zoom(pointer1, pointer2, vector1, vector2, center1, center2);
                // }
            } else // if (vector2.vector[Vector2D.MAGNITUDE] < moveThreshold) {
                   // If one touch is very small then count the other.
                   // TODO Maybe use magnitude as a factor and weigh angles together
            if ((angle1 > 135) || (angle1 < 45)) {
                zoom(pointer1, pointer2, vector1, vector2, center1, center2);
            }
            // }

            // }
        }

    }

    private void zoom(PointerMotionData pointer1, PointerMotionData pointer2, Vector2D vector1, Vector2D vector2,
            Vector2D center1, Vector2D center2) {
        // Zoom movement
        sendToListeners(new MMIPointerEvent(pointer1, pointer2, vector1, vector2, vector1.dot(center1),
                vector2.dot(center2)));

    }

    /**
     * Internal method to fetch the pointer motion delta as 2D vector
     * 
     * @param motionData
     * @param count Number of prior pointer values to include in delta.
     * @return Pointer delta values as Vector2D, or null if only 1 pointer data.
     */
    private Vector2D getDeltaAsVector(PointerMotionData motionData, int count) {
        float[] delta = motionData.getDelta(count);
        if (delta == null || (delta[0] == 0 && delta[1] == 0)) {
            return null;
        }
        return new Vector2D(delta);
    }

    /**
     * Adds the listener, it will now get MMIEvents
     * 
     * @param listener
     */
    public void addMMIListener(MMIEventListener listener) {
        mmiListeners.add(listener);
        SimpleLogger.d(getClass(), "Added listener, new total: " + mmiListeners.size());
    }

    /**
     * Removes the {@linkplain MMIEventListener}, the listener will no longer get mmi callbacks
     * 
     * @param listener
     */
    public void removeMMIListener(MMIEventListener listener) {
        mmiListeners.remove(listener);
        SimpleLogger.d(getClass(), "Removed listener, new total: " + mmiListeners.size());
    }

    /**
     * Sends the event to listeners, if event is specified. If null, nothing is done.
     * 
     * @param event
     */
    private void sendToListeners(MMIPointerEvent event) {
        if (event == null) {
            return;
        }
        for (MMIEventListener listener : mmiListeners) {
            listener.onInputEvent(event);
        }

    }

    /**
     * Sets the scale factor for incoming pointer values, a value of 1 will keep the values.
     * Use 1/width and 1/height to normalize pointer values.
     * 
     * @param scaleX
     * @param scaleY
     * @param translateX Offset for the x position
     * @param translateY Offset for y position
     */
    public void setPointerTransform(float scaleX, float scaleY, float translateX, float translateY) {
        transform[X] = scaleX;
        transform[Y] = scaleY;
        transform[2] = translateX;
        transform[3] = translateY;
    }

    /**
     * Returns the pointer scale in x and y
     * 
     * @param scale Array where x and y scale are written
     */
    public void getPointerScale(float[] scale) {
        scale[0] = transform[X];
        scale[1] = transform[Y];
    }
}
