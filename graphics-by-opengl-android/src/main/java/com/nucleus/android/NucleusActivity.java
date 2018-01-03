package com.nucleus.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.nucleus.CoreApp;
import com.nucleus.SimpleLogger;
import com.nucleus.common.Constants;
import com.nucleus.common.Environment;
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
import android.view.View;
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

    public final static String SYSTEM_PROPERTY = "/system/bin/getprop";
    
    protected SurfaceView surfaceView;
    private static Throwable throwable;
    private static NucleusActivity activity;

    protected CoreApp coreApp;
    protected Class<?> clientClass;
    protected GLESWrapper gles;
    private long androidUptimeDelta;
    /**
     * Set to false to use GLSurfaceView
     * Hint to subclasses before creating SurfaceView
     */
    protected boolean useEGL14 = true;
    
    /**
     * EGL swap interval, must use egl surfaceview for this to work.
     */
    protected int eglSwapInterval = 1;
    
    /**
     * Hint to subclasses
     */
    protected int samples = Constants.NO_VALUE;
    /**
     * Hint to subclasses
     */
    protected int eglSleep = Constants.NO_VALUE;
    /**
     * Hint to subclasses
     */
    protected boolean eglWaitClient = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SimpleLogger.setLogger(new AndroidLogger());
        SimpleLogger.d(getClass(), "onCreate()");
        if (clientClass == null) {
            throw new IllegalArgumentException("ClientClass must be set before calling super.onCreate()");
        }
        activity = this;
        super.onCreate(savedInstanceState);
        checkProperties();
        setup(getRenderVersion(), GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onResume() {
        SimpleLogger.d(getClass(), "onResume()");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        surfaceView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
     * Checks for set properties and updates related fields.
     * Currently checks for EGL/GL Surface usage.
     */
    protected void checkProperties() {
        Environment e = Environment.getInstance();
        String egl = readProperty(Environment.Property.EGL14SURFACE.name());
        useEGL14 = egl != null && egl.length() > 0 ? Boolean.parseBoolean(egl) : useEGL14;
        String s = readProperty(Environment.Property.SAMPLES.name());
        samples = s != null && s.length() != 0 ? Integer.parseInt(s) : Constants.NO_VALUE;
        s = readProperty(Environment.Property.EGLSLEEP.name());
        eglSleep = (s != null && s.length() > 0) ? Integer.parseInt(s) : Constants.NO_VALUE;
        s = readProperty(Environment.Property.EGLWAITCLIENT.name());
        eglWaitClient = s != null && s.length() > 0 ? Boolean.parseBoolean(s) : eglWaitClient;
        s = readProperty(Environment.Property.EGLSWAPINTERVAL.name());
        eglSwapInterval = s != null && s.length() > 0 ? Integer.parseInt(s) : eglSwapInterval;
        e.setProperty(Environment.Property.EGLWAITGL, readProperty(Environment.Property.EGLWAITGL.name()));
        SimpleLogger.d(getClass(), "useEGL14=" + useEGL14 + ", samples=" + samples + ", eglSleep=" + eglSleep + ", eglWaitClient=" + eglWaitClient + ", eglSwapInterval=" + eglSwapInterval + ", eglWaitGL=" + e.getProperty(Environment.Property.EGLWAITGL));
    }

    /**
     * Reads a system property with the specified key, key is converted to lowercase.
     * @param key
     * @return The property key or null
     */
    public static String readProperty(String key) {
        try {
            Process proc = Runtime.getRuntime().exec(new String[] { SYSTEM_PROPERTY, key.toLowerCase() });
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            SimpleLogger.d(NucleusActivity.class, "Exception reading property: " + e);
        }
        return null;
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
        SimpleLogger.d(getClass(), "Using " + surfaceView.getClass().getSimpleName());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(surfaceView);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        com.nucleus.renderer.Window.getInstance().setScreenSize(size.x, size.y);
        androidUptimeDelta = System.currentTimeMillis() - android.os.SystemClock.uptimeMillis();
    }

    /**
     * Creates the SurfaceView to be used to render GL content.
     * To use EGL1.4 use {@link EGLSurfaceView} or subclass.
     * To use legacy EGL1.0/1.1
     * 
     * @param version
     * @param surfaceConfig
     * @param rendermode
     * @return
     */
    protected SurfaceView createSurfaceView(Renderers version, SurfaceConfiguration surfaceConfig, int rendermode) {
        if (useEGL14) {
            return new EGLSurfaceView(surfaceConfig, version, this);
        } else {
            return new AndroidSurfaceView(surfaceConfig, version, this);
        }
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
     * Called when an EGL surface for rendering has been created, this will call to create core app and display splash.
     * Swap buffers after this method returns to make splash visible.
     * Then call {@link CoreApp#contextCreated(int, int)}
     * 
     * @param width
     * @param height
     */
    public void onSurfaceCreated(int width, int height) {
        NucleusRenderer renderer = RendererFactory.getRenderer(gles, new AndroidImageFactory(),
                new AndroidMatrixEngine());
        coreApp = CoreApp.createCoreApp(width, height, renderer, clientClass);
    }

    /**
     * Call {@link CoreApp#contextCreated(int, int)} - this signalls that context is created and everything is ready to
     * start render
     * 
     * @param width
     * @param height
     */
    public void contextCreated(int width, int height) {
        // Call contextCreated since the renderer is already initialized and has a created EGL context.
        coreApp.contextCreated(width, height);
        if (surfaceView instanceof AndroidSurfaceView) {
            ((AndroidSurfaceView) surfaceView).setRenderContextListener(coreApp);
        } else if (surfaceView instanceof EGLSurfaceView) {
            ((EGLSurfaceView) surfaceView).setRenderContextListener(coreApp);
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
                        new float[] { event.getX(index), event.getY(index) }, event.getPressure());
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // This is multitouch or the last pointer going up
                coreApp.getInputProcessor().pointerEvent(PointerAction.UP, type,
                        event.getEventTime() + androidUptimeDelta,
                        finger, new float[] { event.getX(index), event.getY(index) }, event.getPressure());
                break;
            case MotionEvent.ACTION_MOVE:
                // Handle history
                final int historySize = event.getHistorySize();
                for (int i = 0; i < count; i++) {
                    finger = event.getPointerId(i);
                    for (int h = 0; h < historySize; h++) {
                        coreApp.getInputProcessor().pointerEvent(PointerAction.MOVE, type,
                                event.getHistoricalEventTime(h) + androidUptimeDelta, finger,
                                new float[] { event.getHistoricalX(i, h), event.getHistoricalY(i, h) },
                                event.getHistoricalPressure(index, h));
                    }
                    coreApp.getInputProcessor().pointerEvent(PointerAction.MOVE, type,
                            event.getEventTime() + androidUptimeDelta, finger,
                            new float[] { event.getX(i), event.getY(i) }, event.getPressure());
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
