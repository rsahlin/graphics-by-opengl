package com.nucleus.android;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.mmi.PointerInputProcessor;

public class AndroidSurfaceView extends GLSurfaceView {

    PointerInputProcessor inputProcessor;

    public AndroidSurfaceView(Context context, Renderer renderer, PointerInputProcessor inputProcessor) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        this.inputProcessor = inputProcessor;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int count = event.getPointerCount();
        for (int i = 0; i < count; i++) {
            int finger = event.getPointerId(i);
            int actionFinger = event.getActionIndex();
            switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                // ACTION_POINTER_DOWN is called for all pointers in case of multi touch since it record new current
                // touch position for already down pointers - ignore the first fingers.
                if (finger != actionFinger) {
                    break;
                }
            case MotionEvent.ACTION_DOWN:
                // We must implement code in both ACTION_DOWN and ACTION_POINTER_DOWN, ACTION_DOWN is called for first
                // pointer
                inputProcessor.pointerEvent(PointerAction.DOWN, event.getEventTime(), finger,
                        new float[] { event.getX(i), event.getY(i) });
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // ACTION_POINTER_UP is called for all pointers in case of multi touch since it record new current
                // touch position for already down pointers - ignore the first fingers.
                if (finger != actionFinger) {
                    break;
                }
            case MotionEvent.ACTION_UP:
                // We must implement code in both ACTION_UP and ACTION_POINTER_UP, ACTION_UP is called for first pointer
                inputProcessor.pointerEvent(PointerAction.UP, event.getEventTime(), finger, new float[] {
                        event.getX(i), event.getY(i) });
                break;
            case MotionEvent.ACTION_MOVE:
                inputProcessor.pointerEvent(PointerAction.MOVE, event.getEventTime(), finger,
                        new float[] { event.getX(i), event.getY(i) });
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
            }
        }
        requestRender();
        return true;
    }
}