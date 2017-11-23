package com.nucleus.android;

import com.nucleus.CoreApp;
import com.nucleus.SimpleLogger;
import com.nucleus.matrix.android.AndroidMatrixEngine;
import com.nucleus.mmi.PointerData;
import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.mmi.PointerData.Type;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RendererFactory;
import com.nucleus.renderer.SurfaceConfiguration;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.texture.android.AndroidImageFactory;
import com.super2k.nucleus.android.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;
import android.view.Display.Mode;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

/**
 * Base activity to get NucleusRenderer functionality on Android.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class NucleusActivity extends Activity
        implements DialogInterface.OnClickListener {

    protected EGLSurfaceView EGLSurface;
    protected SurfaceView surfaceView;
    private static Throwable throwable;
    private static NucleusActivity activity;

    protected CoreApp coreApp;
    protected Class<?> clientClass;
    protected GLESWrapper gles;
    private long androidUptimeDelta;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SimpleLogger.setLogger(new AndroidLogger());
        SimpleLogger.d(getClass(), "onCreate()");
        if (clientClass == null) {
            throw new IllegalArgumentException("ClientClass must be set before calling super.onCreate()");
        }
        activity = this;
        super.onCreate(savedInstanceState);
        setup(getRenderVersion(), GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onResume() {
        SimpleLogger.d(getClass(), "onResume()");
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (coreApp != null) {
            if (coreApp.onBackPressed()) {
                coreApp.setDestroyFlag();
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        coreApp.setDestroyFlag();
        SimpleLogger.d(getClass(), "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onStart() {
        SimpleLogger.d(getClass(), "onStart()");
        super.onStart();
    }

    /**
     * Returns the version of the renderer to use
     * 
     * @return
     */
    public abstract Renderers getRenderVersion();

    /**
     * Returns the number of samples to use for the EGL config.
     * 
     * @return
     */
    public abstract int getSamples();

    /**
     * Setup this activity with a new GLSurfaceView create with the specified renderer
     * When this method returns the created view is the active content view (ie visible)
     * 
     * @param version
     * @param rendermode
     */
    private void setup(Renderers version, int rendermode) {
        SurfaceConfiguration surfaceConfig = createSurfaceConfig();
        createWrapper(version);
        surfaceConfig.setSamples(getSamples());
        surfaceView = createSurfaceView(version, surfaceConfig, rendermode);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(surfaceView);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        com.nucleus.renderer.Window.getInstance().setScreenSize(size.x, size.y);
        androidUptimeDelta = System.currentTimeMillis() - android.os.SystemClock.uptimeMillis();
    }

    protected SurfaceView createSurfaceView(Renderers version, SurfaceConfiguration surfaceConfig, int rendermode) {

        return new EGLSurfaceView(surfaceConfig, version, this);

        // SurfaceView sf = new AndroidSurfaceView(surfaceConfig, version, this);
        // ((GLSurfaceView) sf).setRenderMode(rendermode);
        // return sf;
    }

    /**
     * Creates the wanted EGL surface configuration, creates a default {@link SurfaceConfiguration}
     * 
     * @return
     */
    protected SurfaceConfiguration createSurfaceConfig() {
        return new SurfaceConfiguration();
    }

    private void createWrapper(Renderers version) {
        switch (version) {
            case GLES20:
                gles = new AndroidGLES20Wrapper();
                break;
            case GLES30:
                gles = new AndriodGLES30Wrapper();
                break;
            default:
                throw new IllegalArgumentException("Not implemented for version:" + version);
        }
    }

    public static void handleThrowable(final Throwable t) {
        throwable = t;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    activity.showAlert(activity.getString(R.string.app_name) + " " + activity.getString(R.string.crash),
                            t.toString());
                } catch (Throwable t2) {
                    throw new RuntimeException(throwable);
                }
            }
        });
    }

    /**
     * Will display alert message with neutral button to dismiss.
     * Use this in case of unrecoverable error, for instance server error.
     * MUST be called from the UI thread!
     * 
     * @param title
     * @param messag
     */
    protected void showAlert(final String title, final String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message).setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getText(R.string.ok), this);
        alertDialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (throwable != null) {
            throw new RuntimeException(throwable);
        }
        finish();
    }

    @TargetApi(23)
    protected Mode get4KMode() {
        Mode closest = null;
        Mode[] modes = getWindowManager().getDefaultDisplay().getSupportedModes();
        SimpleLogger.d(getClass(), "Found " + modes.length + " modes.");
        for (Mode mode : modes) {
            SimpleLogger.d(getClass(), "Found mode with " + mode.getPhysicalHeight() + " lines.");
            if (mode.getPhysicalHeight() == RESOLUTION.ULTRA_HD.lines) {
                return mode;
            }
            if (mode.getPhysicalHeight() > RESOLUTION.ULTRA_HD.lines) {
                if (closest == null || closest.getPhysicalHeight() > mode.getPhysicalHeight()) {
                    closest = mode;
                }
            }
        }
        return closest;
    }

    /**
     * Called when an EGL surface for rendering has been created
     * 
     */
    public void onSurfaceCreated(int width, int height) {
        NucleusRenderer renderer = RendererFactory.getRenderer(gles, new AndroidImageFactory(),
                new AndroidMatrixEngine());
        coreApp = CoreApp.createCoreApp(width, height, renderer, clientClass);
        // Call contextCreated since the renderer is already initialized and has a created EGL context.
        coreApp.contextCreated(width, height);
        if (surfaceView instanceof AndroidSurfaceView) {
            ((AndroidSurfaceView) surfaceView).setCoreApp(coreApp);
        } else if (surfaceView instanceof EGLSurfaceView) {
            ((EGLSurfaceView) surfaceView).setCoreApp(coreApp);
        }
    }

    protected void handleTouch(MotionEvent event) {
        int index = event.getActionIndex();
        Type type = getType(event.getToolType(index));
        int count = event.getPointerCount();
        int finger = event.getPointerId(index);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                // This is the first pointer, or multitouch pointer going down.
                coreApp.getInputProcessor().pointerEvent(PointerAction.DOWN, type,
                        event.getEventTime() + androidUptimeDelta, finger,
                        new float[] { event.getX(index), event.getY(index) });
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // This is multitouch or the last pointer going up
                coreApp.getInputProcessor().pointerEvent(PointerAction.UP, type,
                        event.getEventTime() + androidUptimeDelta,
                        finger, new float[] { event.getX(index), event.getY(index) });
                break;
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < count; i++) {
                    finger = event.getPointerId(i);
                    coreApp.getInputProcessor().pointerEvent(PointerAction.MOVE, type,
                            event.getEventTime() + androidUptimeDelta, finger,
                            new float[] { event.getX(i), event.getY(i) });
                }
                break;
            default:
        }
    }

    private Type getType(int type) {
        switch (type) {
            case MotionEvent.TOOL_TYPE_ERASER:
                return PointerData.Type.ERASER;
            case MotionEvent.TOOL_TYPE_FINGER:
                return PointerData.Type.FINGER;
            case MotionEvent.TOOL_TYPE_MOUSE:
                return PointerData.Type.MOUSE;
            case MotionEvent.TOOL_TYPE_STYLUS:
                return PointerData.Type.STYLUS;
            default:
                return PointerData.Type.MOUSE;
        }
    }

}
