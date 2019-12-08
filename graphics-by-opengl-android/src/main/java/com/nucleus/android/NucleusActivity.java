package com.nucleus.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow.Configuration;
import com.nucleus.J2SEWindow.VideoMode;
import com.nucleus.SimpleLogger;
import com.nucleus.common.Constants;
import com.nucleus.common.Environment;
import com.nucleus.mmi.Pointer;
import com.nucleus.mmi.Pointer.PointerAction;
import com.nucleus.mmi.Pointer.Type;
import com.nucleus.mmi.core.CoreInput;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.RendererFactory;
import com.nucleus.renderer.RendererInfo;
import com.nucleus.renderer.SurfaceConfiguration;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.texture.android.AndroidImageFactory;
import com.nucleus.texturing.BaseImageFactory;
import com.super2k.nucleus.android.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Display.Mode;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
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
    public final static String CHOREOGRAPHER_KEY = "choreographer";

    protected SurfaceView surfaceView;
    private static Throwable throwable;
    private static NucleusActivity activity;

    protected CoreApp coreApp;
    protected GLESWrapper gles;
    protected Renderers minVersion;
    private long androidUptimeDelta;
    /**
     * Set to false to use GLSurfaceView
     * Hint to subclasses before creating SurfaceView
     */
    protected boolean useEGL14 = true;

    /**
     * EGL swap interval, must use egl surfaceview for this to work. Set to eglSurfaceView
     * Change here to change the default value, may be overridden by property.
     */
    protected int eglSwapInterval = 1;
    /**
     * True to use choreographer for rendering on UI thread.
     * Change here to change the default value, may be overridden by property.
     */
    protected boolean useChoreographer = true;

    /**
     * Surface attributes for eglCreateWindows, must use egl surfaceview for this to work. Set to eglSurfaceView
     */
    protected int[] surfaceAttribs;

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

    /**
     * If true this activity is moved to background in {@link #onBackPressed()} otherwise activity is finished.
     */
    protected boolean backgroundTaskOnBackPressed = true;

    protected SurfaceConfiguration surfaceConfig;
    protected Configuration configuration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SimpleLogger.setLogger(new AndroidLogger());
        SimpleLogger.d(getClass(), "onCreate()");
        BaseImageFactory.setFactory(new AndroidImageFactory());
        activity = this;
        checkProperties();
        setup(getRenderVersion(), GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        super.onCreate(savedInstanceState);
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
        if (backgroundTaskOnBackPressed) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
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
        String eglStr = readProperty(Environment.Property.EGL14SURFACE.name());
        useEGL14 = eglStr != null && eglStr.length() > 0 ? Boolean.parseBoolean(eglStr) : useEGL14;
        String samplesStr = readProperty(Environment.Property.SAMPLES.name());
        samples = samplesStr != null && samplesStr.length() != 0 ? Integer.parseInt(samplesStr) : Constants.NO_VALUE;
        String sleepStr = readProperty(Environment.Property.EGLSLEEP.name());
        eglSleep = (sleepStr != null && sleepStr.length() > 0) ? Integer.parseInt(sleepStr) : Constants.NO_VALUE;
        String waitclientStr = readProperty(Environment.Property.EGLWAITCLIENT.name());
        eglWaitClient = waitclientStr != null && waitclientStr.length() > 0 ? Boolean.parseBoolean(waitclientStr)
                : eglWaitClient;
        String swapIntervalStr = readProperty(Environment.Property.SWAPINTERVAL.name());
        eglSwapInterval = swapIntervalStr != null && swapIntervalStr.length() > 0 ? Integer.parseInt(swapIntervalStr)
                : eglSwapInterval;
        String choreographerStr = readProperty(CHOREOGRAPHER_KEY);
        useChoreographer = choreographerStr != null && choreographerStr.length() > 0
                ? Boolean.parseBoolean(choreographerStr)
                : useChoreographer;
        for (Environment.Property p : Environment.Property.values()) {
            String str = readProperty(p.name());
            if (str != null && str.length() > 0) {
                e.setProperty(p, readProperty(p.name()));
            }
        }
        SimpleLogger.d(getClass(),
                "useEGL14=" + useEGL14 + " (property=" + eglStr + "), samples=" + samples + " (property=" + samplesStr
                        + "), eglSleep=" + eglSleep + " (property=" + sleepStr + "), eglWaitClient=" + eglWaitClient
                        + " (property=" + waitclientStr + ") , eglSwapInterval=" + eglSwapInterval + " (property="
                        + swapIntervalStr + "), eglWaitGL=" + e.getProperty(Environment.Property.EGLWAITGL)
                        + " useChoreograper=" + useChoreographer + " (" + choreographerStr + ")");
    }

    /**
     * Reads a system property with the specified key, key is converted to lowercase.
     * 
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
     * Returns the version of the renderer to use - this will affect what shading language is supported.
     * 
     * @return
     */
    public abstract Renderers getRenderVersion();

    /**
     * Returns the number of samples to use for the EGL config.
     * This is called from the {@link #createSurfaceConfig()} and used when setting number of samples in the
     * surfaceconfig.
     * 
     * @return Number of wanted samples
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
        minVersion = version;
        surfaceConfig = createSurfaceConfig();
        SimpleLogger.d(getClass(), "Using SurfaceConfig:\n" + surfaceConfig.toString());
        surfaceView = createSurfaceView(version, surfaceConfig, rendermode);
        surfaceView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        SimpleLogger.d(getClass(), "Using " + surfaceView.getClass().getSimpleName());
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(surfaceView);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        androidUptimeDelta = java.lang.System.currentTimeMillis() - android.os.SystemClock.uptimeMillis();

    }

    /**
     * Creates the SurfaceView to be used to render GL content.
     * To use EGL1.4 use {@link EGLSurfaceView} or subclass.
     * To use legacy EGL1.0/1.1 use {@link AndroidSurfaceView}
     * This method is called from {@link #onCreate(Bundle)}
     * 
     * @param version
     * @param surfaceConfig
     * @param rendermode
     * @return
     */
    protected SurfaceView createSurfaceView(Renderers version, SurfaceConfiguration surfaceConfig, int rendermode) {
        if (useEGL14 && Build.VERSION.SDK_INT > 16) {
            return new EGLSurfaceView(surfaceConfig, version, this, eglSwapInterval,
                    surfaceAttribs, useChoreographer);
        } else {
            return new AndroidSurfaceView(surfaceConfig, version, this);
        }
    }

    /**
     * Creates the wanted EGL surface configuration, creates a default {@link SurfaceConfiguration}
     * then sets number of samples with value from {@link #getSamples()}
     * 
     * @return
     */
    protected SurfaceConfiguration createSurfaceConfig() {
        SurfaceConfiguration surfaceConfig = new SurfaceConfiguration();
        surfaceConfig.setSamples(getSamples());
        return surfaceConfig;
    }

    private void createWrapper(Renderers version) {
        int[] glVersion = RendererInfo.getVersionStr(android.opengl.GLES20.glGetString(GLES20.GL_VERSION));
        Renderers runtime = Renderers.get(glVersion);
        SimpleLogger.d(getClass(), "Found GLES runtime version: " + runtime);
        if (version.major > runtime.major || version.minor > runtime.minor) {
            throw new IllegalArgumentException("Must support at least GLES version " + version);
        }
        switch (runtime) {
            case GLES20:
                gles = new AndroidGLES20Wrapper();
                break;
            case GLES30:
                gles = new AndroidGLES30Wrapper();
                break;
            case GLES31:
                gles = new AndroidGLES31Wrapper();
                break;
            case GLES32:
                gles = new AndroidGLES32Wrapper();
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

    @TargetApi(27)
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
     * Called when an EGL surface for rendering has been created.
     * If coreApp is null it is created and splash is displayed.
     * Swap buffers after this method returns to make splash visible, if true is returned.
     * Then call {@link CoreApp#contextCreated(int, int)}
     * 
     * @param width
     * @param height
     * @return true if CoreApp was created and splash displayed.
     */
    public boolean onSurfaceCreated(int width, int height) {
        if (gles == null) {
            createWrapper(minVersion);
        }
        NucleusRenderer renderer = RendererFactory.getRenderer(gles);
        if (coreApp == null) {
            if (configuration == null) {
                configuration = new Configuration(minVersion, surfaceConfig, new VideoMode(width, height));
            }
            coreApp = CoreApp.createCoreApp(renderer, configuration);
            return true;
        }
        return false;
    }

    /**
     * Call {@link CoreApp#contextCreated(int, int)} - this signals that context is created and everything is ready to
     * start render
     * 
     * @param width
     * @param height
     */
    public void contextCreated(int width, int height) {
        // Call contextCreated since the renderer is already initialized and has a created EGL context.
        coreApp.contextCreated(width, height);
        if (surfaceView instanceof AndroidSurfaceView) {
            createdSurfaceView((AndroidSurfaceView) surfaceView);
        } else if (surfaceView instanceof EGLSurfaceView) {
            createdEGLSurfaceView((EGLSurfaceView) surfaceView);
        }
    }

    /**
     * Called after {@link CoreApp#contextCreated(int, int)} is called when {@link AndroidSurfaceView} is used.
     * Subclasses must call super
     * 
     * @param surfaceView
     */
    protected void createdSurfaceView(AndroidSurfaceView surfaceView) {
        surfaceView.setRenderContextListener(coreApp);
    }

    /**
     * Called after {@link CoreApp#contextCreated(int, int)} is called when {@link EGLSurfaceView} is used.
     * Subclasses must call super
     * 
     * @param surfaceView
     */
    protected void createdEGLSurfaceView(EGLSurfaceView surfaceView) {
        surfaceView.setRenderContextListener(coreApp);
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
                CoreInput.getInstance().pointerEvent(PointerAction.DOWN, type,
                        event.getEventTime() + androidUptimeDelta, finger,
                        new float[] { event.getX(index), event.getY(index) }, event.getPressure());
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // This is multitouch or the last pointer going up
                CoreInput.getInstance().pointerEvent(PointerAction.UP, type,
                        event.getEventTime() + androidUptimeDelta,
                        finger, new float[] { event.getX(index), event.getY(index) }, event.getPressure());
                break;
            case MotionEvent.ACTION_MOVE:
                // Handle history
                final int historySize = event.getHistorySize();
                for (int i = 0; i < count; i++) {
                    finger = event.getPointerId(i);
                    for (int h = 0; h < historySize; h++) {
                        CoreInput.getInstance().pointerEvent(PointerAction.MOVE, type,
                                event.getHistoricalEventTime(h) + androidUptimeDelta, finger,
                                new float[] { event.getHistoricalX(i, h), event.getHistoricalY(i, h) },
                                event.getHistoricalPressure(index, h));
                    }
                    CoreInput.getInstance().pointerEvent(PointerAction.MOVE, type,
                            event.getEventTime() + androidUptimeDelta, finger,
                            new float[] { event.getX(i), event.getY(i) }, event.getPressure());
                }
                break;
            default:
                SimpleLogger.d(getClass(), "Not handled MotionEvent: " + event.getActionMasked());
        }
    }

    private Type getType(int type) {
        switch (type) {
            case MotionEvent.TOOL_TYPE_ERASER:
                return Pointer.Type.ERASER;
            case MotionEvent.TOOL_TYPE_FINGER:
                return Pointer.Type.FINGER;
            case MotionEvent.TOOL_TYPE_MOUSE:
                return Pointer.Type.MOUSE;
            case MotionEvent.TOOL_TYPE_STYLUS:
                return Pointer.Type.STYLUS;
            default:
                return Pointer.Type.MOUSE;
        }
    }

}
