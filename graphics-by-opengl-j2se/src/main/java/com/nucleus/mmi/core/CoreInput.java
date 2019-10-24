package com.nucleus.mmi.core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.nucleus.SimpleLogger;
import com.nucleus.geometry.Vertex2D;
import com.nucleus.mmi.Key;
import com.nucleus.mmi.MMIPointer;
import com.nucleus.mmi.MMIPointerInput;
import com.nucleus.mmi.Pointer;
import com.nucleus.mmi.Pointer.PointerAction;
import com.nucleus.mmi.Pointer.Type;
import com.nucleus.mmi.PointerMotion;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.profiling.FrameSampler.Sample;
import com.nucleus.profiling.FrameSampler.Samples;
import com.nucleus.renderer.Window;
import com.nucleus.vecmath.Vec2;

/**
 * Process raw incoming pointer based events (for instance touch or mouse) and turn into easier to handle MMI actions.
 * Handles raw key events by passing them on to registered keylisteners
 * Is used by platform implementations to turn platform input events, such as mouse or touch, into {@link MMIPointer}
 * input data.
 * This is a singleton class that shall be fetched by calling {@link #getInstance()}
 * To listen to fullscreen pointer input events regardless of bounds checks, call
 * {@link #addMMIListener(MMIPointerInput)}
 * 
 */
public class CoreInput implements RawPointerInput, KeyInput {

    public int maxPointers = 5;

    /**
     * Pointer motion data, one for each supported pointer.
     */
    PointerMotion[] pointerMotionData;

    /**
     * Scale and offset value for incoming pointer values, this can be used to normalize pointer or align them with
     * the size of the viewport.
     * Set to 1/width and 1/height to normalize.
     * Values are: scalex, scaley, translatex, translatey
     */
    private final float[] transform = new float[] { 1, 1, 0, 0 };
    private final float[] scaledPosition = new float[2];

    Set<MMIPointerInput> mmiListeners = new HashSet<>();
    Set<KeyInput> keyListeners = new HashSet<>();

    private int pointerCount = 0;
    /**
     * If a movement is less than this then don't count. Use to filter out too small movements.
     */
    private float moveThreshold = 3;

    /**
     * Set to false to disable actions from two pointers, ZOOM
     * When this is enabled normal MOVE events will not be sent when 2 or more pointers are active.
     */
    private boolean processTwoPointers = true;

    /**
     * Keeps track of pressed keyvalues - this will be same as the java.awt.KeyEvent values.
     */
    private LinkedList<Integer> keyCodes = new LinkedList<>();

    private static CoreInput inputProcessor;

    /**
     * Returns the singleton instance of the input processor.
     * 
     * @return
     */
    public static CoreInput getInstance() {
        if (inputProcessor == null) {
            inputProcessor = new CoreInput();
        }
        return inputProcessor;
    }

    private CoreInput() {
        pointerMotionData = new PointerMotion[maxPointers];
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
    public void pointerEvent(PointerAction action, Type type, long timestamp, int pointer, float[] position,
            float pressure) {
        if (pointer >= maxPointers) {
            return;
        }
        Sample sample = getSample();
        long start = System.nanoTime();
        scaledPosition[X] = position[X] * transform[X] + transform[2];
        scaledPosition[Y] = position[Y] * transform[Y] + transform[3];
        switch (action) {
            case MOVE:
                if (pointerMotionData[pointer].getCurrent().action == Pointer.PointerAction.UP) {
                    SimpleLogger.d(getClass(), "Move after up");
                } else {
                    // More than one pointer is or has been active.
                    if (processTwoPointers && pointerCount >= 2) {
                        pointerMotionData[pointer].add(
                                pointerMotionData[pointer].create(action, type, timestamp, pointer, scaledPosition,
                                        pressure));
                        if (pointer == 1) {
                            processTwoPointers();
                        }
                        break;
                    }
                    addAndSend(new MMIPointer(com.nucleus.mmi.MMIPointer.Action.MOVE, pointer,
                            pointerMotionData[pointer]),
                            pointerMotionData[pointer].create(action, type, timestamp, pointer, scaledPosition,
                                    pressure));
                }
                break;
            case DOWN:
                pointerMotionData[pointer] = new PointerMotion();
                addAndSend(new MMIPointer(com.nucleus.mmi.MMIPointer.Action.ACTIVE, pointer,
                        pointerMotionData[pointer]),
                        pointerMotionData[pointer].create(action, type, timestamp, pointer, scaledPosition, pressure));
                pointerCount = getActivePointerCount();
                break;
            case UP:
                if (pointerCount < 0) {
                    SimpleLogger.d(getClass(), "PointerInputProcessor: ERROR: pointerCount= " + pointerCount);
                }
                addAndSend(new MMIPointer(com.nucleus.mmi.MMIPointer.Action.INACTIVE, pointer,
                        pointerMotionData[pointer]),
                        pointerMotionData[pointer].create(action, type, timestamp, pointer, scaledPosition, pressure));
                pointerCount--;
                break;
            case ZOOM:
                if (pointerMotionData[pointer] == null) {
                    pointerMotionData[pointer] = new PointerMotion();
                }
                MMIPointer zoom = new MMIPointer(com.nucleus.mmi.MMIPointer.Action.ZOOM, pointer,
                        pointerMotionData[pointer]);
                float x = position[X] * transform[X];
                float y = position[Y] * transform[Y];
                zoom.setZoom(x, y);
                addAndSend(zoom,
                        pointerMotionData[pointer].create(action, type, timestamp, pointer, x, y, pressure));
                break;
            default:
                throw new IllegalArgumentException();
        }
        sample.addNano((int) (System.nanoTime() - start));
    }

    private int getActivePointerCount() {
        int count = 0;
        for (int i = 0; i < pointerMotionData.length; i++) {
            if (pointerMotionData[i] != null && pointerMotionData[i].isDown()) {
                count++;
            }
        }
        return count;
    }

    private Sample getSample() {
        Sample sample = FrameSampler.getInstance().getSample(Samples.POINTER_INPUT.name());
        if (sample == null) {
            sample = new Sample();
            FrameSampler.getInstance().setSample(Samples.POINTER_INPUT.name(), sample);
        }
        return sample;
    }

    private void addAndSend(MMIPointer event, Pointer pointerData) {
        pointerMotionData[pointerData.pointer].add(pointerData);
        sendToListeners(event);
    }

    private boolean processTwoPointers() {
        PointerMotion pointer1 = pointerMotionData[Pointer.POINTER_1];
        PointerMotion pointer2 = pointerMotionData[Pointer.POINTER_2];
        // Find point between the 2 points.
        float[] middle = Vertex2D.middle(pointer1.getFirstPosition(), pointer2.getFirstPosition());
        float[] toMiddle = new float[2];
        float[] toMiddle2 = new float[2];
        // Fetch touch movement as 2D vectors
        Vec2 vector1 = getDeltaAsVector(pointer1, 1);
        Vec2 vector2 = getDeltaAsVector(pointer2, 1);
        // Check if movement from or towards middle.
        if (vector1 != null && vector2 != null) {
            Vertex2D.getDistance(middle, pointer1.getCurrentPosition(), toMiddle);
            Vec2 center1 = new Vec2(toMiddle);
            Vertex2D.getDistance(middle, pointer2.getCurrentPosition(), toMiddle2);
            Vec2 center2 = new Vec2(toMiddle2);

            float angle1 = (float) Math.acos(vector1.dot(center1)) * 57.2957795f;
            float angle2 = (float) Math.acos(vector2.dot(center2)) * 57.2957795f;
            if ((angle1 > 135 && angle2 > 135) || (angle1 < 45 && angle2 < 45)) {
                zoom(pointer1, pointer2, vector1, vector2, center1, center2);
                return true;
            } else // if (vector1.vector[Vector2D.MAGNITUDE] < moveThreshold) {
                   // If one touch is very small then count the other.
                   // TODO Maybe use magnitude as a factor and weigh angles together
            if ((angle2 > 135) || (angle2 < 45)) {
                zoom(pointer1, pointer2, vector1, vector2, center1, center2);
                return true;
                // }
            } else // if (vector2.vector[Vector2D.MAGNITUDE] < moveThreshold) {
                   // If one touch is very small then count the other.
                   // TODO Maybe use magnitude as a factor and weigh angles together
            if ((angle1 > 135) || (angle1 < 45)) {
                zoom(pointer1, pointer2, vector1, vector2, center1, center2);
                return true;
            }
            // }

            // }
        }
        return false;
    }

    private void zoom(PointerMotion pointer1, PointerMotion pointer2, Vec2 vector1, Vec2 vector2,
            Vec2 center1, Vec2 center2) {
        // Zoom movement
        MMIPointer zoom = new MMIPointer(pointer1, pointer2, vector1, vector2, vector1.dot(center1),
                vector2.dot(center2));
        float[] vector = zoom.getZoom().vector;
        addAndSend(zoom,
                pointer1.create(PointerAction.ZOOM, Pointer.Type.FINGER, System.currentTimeMillis(),
                        Pointer.POINTER_1, vector[0] * vector[2], vector[1] * vector[2], 1f));
    }

    /**
     * Internal method to fetch the pointer motion delta as 2D vector - magnitude will be scaled to fraction of screen
     * width/height so that a value of 0.1 means 1/10 of width/height
     * 
     * @param motionData
     * @param count Number of prior pointer values to include in delta.
     * @return Pointer delta values as Vector2D, or null if only 1 pointer data.
     */
    private Vec2 getDeltaAsVector(PointerMotion motionData, int count) {
        float[] delta = motionData.getDelta(count);
        if (delta == null || (delta[0] == 0 && delta[1] == 0)) {
            return null;
        }
        int height = Window.getInstance().getHeight();
        Vec2 deltaVec = new Vec2(delta);
        deltaVec.vector[Vec2.MAGNITUDE] = deltaVec.vector[Vec2.MAGNITUDE] / (getPointerScaleY() * height);
        return deltaVec;
    }

    /**
     * Adds the listener, it will now get MMIEvents from the whole window regardless of element bound checks.
     * Use this to capture incoming pointers in a fullscreen scenario.
     * 
     * @param listener
     */
    public void addMMIListener(MMIPointerInput listener) {
        mmiListeners.add(listener);
        SimpleLogger.d(getClass(), "Added MMI listener, new total: " + mmiListeners.size());
    }

    /**
     * Adds the keylistener, it will not get key events
     * 
     * @param listener
     */
    public void addKeyListener(KeyInput listener) {
        keyListeners.add(listener);
        SimpleLogger.d(getClass(), "Added key listener, new total: " + keyListeners.size());
    }

    /**
     * Removes the {@linkplain MMIPointerInput}, the listener will no longer get mmi callbacks
     * 
     * @param listener
     */
    public void removeMMIListener(MMIPointerInput listener) {
        mmiListeners.remove(listener);
        SimpleLogger.d(getClass(), "Removed MMI listener, new total: " + mmiListeners.size());
    }

    /**
     * Removes the key listener
     * 
     * @param listener
     */
    public void removeKeyListener(KeyInput listener) {
        keyListeners.remove(listener);
        SimpleLogger.d(getClass(), "Removed key listener, new total: " + keyListeners.size());
    }

    /**
     * Sends the event to listeners, if event is specified. If null, nothing is done.
     * 
     * @param event
     */
    private void sendToListeners(MMIPointer event) {
        if (event == null) {
            return;
        }
        for (MMIPointerInput listener : mmiListeners) {
            listener.onInput(event);
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
        SimpleLogger.d(getClass(),
                "Pointer transform, scale: " + scaleX + ", " + scaleY + " : Offset " + translateX + ", " + translateY);
        transform[X] = scaleX;
        transform[Y] = scaleY;
        transform[2] = translateX;
        transform[3] = translateY;
    }

    /**
     * Returns the pointer scale in Y - this is the factor between screen coordinates and touch coordinates
     * returned as absolute value.
     * 
     * @return Pointer scale in y as a positive value
     */
    public float getPointerScaleY() {
        return Math.abs(transform[Y]);
    }

    /**
     * Returns the max number of pointers that will be reported
     * 
     * @return
     */
    public int getMaxPointers() {
        return maxPointers;
    }

    /**
     * Sets the max number of pointers that will be reported
     * 
     * @param maxPointers
     */
    public void setMaxPointers(int maxPointers) {
        this.maxPointers = maxPointers;
        pointerMotionData = new PointerMotion[maxPointers];
    }

    /**
     * Reverses the pointer normalization
     * 
     * @param normalized
     * @return
     */
    public float[] getScreenCoordinate(float[] normalized) {
        return new float[] { (normalized[0] - transform[2]) / transform[0],
                (normalized[1] - transform[3]) / transform[1] };
    }

    @Override
    public void onKeyEvent(Key event) {
        switch (event.getAction()) {
            case PRESSED:
                keyCodes.add(event.getKeyValue());
                break;
            case RELEASED:
                keyCodes.remove(new Integer(event.getKeyValue()));
                break;
            default:
                throw new IllegalArgumentException("Not implemented for action " + event.getAction());
        }
        for (KeyInput kl : keyListeners) {
            kl.onKeyEvent(event);
        }
    }

    /**
     * Returns true if a key with the key value is pressed
     * 
     * @param keyValue java.awt.KeyEvent value
     * @return
     */
    public boolean isKeyPressed(int keyValue) {
        return keyCodes.contains(keyValue);
    }

}
