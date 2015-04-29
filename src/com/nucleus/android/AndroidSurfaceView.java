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
                // Recording down for multi touch - all pointers will be re-sent when a new finger goes down.
                inputProcessor.pointerEvent(PointerAction.DOWN, event.getEventTime(), finger,
                        new float[] { event.getX(i), event.getY(i) });
                break;
            case MotionEvent.ACTION_DOWN:
                inputProcessor.pointerEvent(PointerAction.DOWN, event.getEventTime(), finger,
                        new float[] { event.getX(i), event.getY(i) });
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (finger == 0) {
                    inputProcessor.pointerEvent(PointerAction.UP, event.getEventTime(), actionFinger, new float[] {
                            event.getX(i), event.getY(i) });
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                inputProcessor.pointerEvent(PointerAction.UP, event.getEventTime(), finger, new float[] {
                        event.getX(i), event.getY(i) });
                break;
            case MotionEvent.ACTION_MOVE:
                inputProcessor.pointerEvent(PointerAction.MOVE, event.getEventTime(), finger,
                        new float[] { event.getX(i), event.getY(i) });
                break;
            default:
            }
        }
        requestRender();
        return true;
    }
}