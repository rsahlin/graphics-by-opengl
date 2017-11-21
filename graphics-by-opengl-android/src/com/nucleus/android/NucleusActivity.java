package com.nucleus.android;

import com.nucleus.CoreApp;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.SimpleLogger;
import com.nucleus.matrix.android.AndroidMatrixEngine;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * Base activity to get NucleusRenderer functionality on Android.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class NucleusActivity extends Activity
        implements DialogInterface.OnClickListener, CoreAppStarter {

    /**
     * Android specific objects
     */
    protected AndroidSurfaceView mGLView;
    private static Throwable throwable;
    private static NucleusActivity activity;

    protected CoreApp coreApp;
    protected Class<?> clientClass;
    protected GLESWrapper gles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SimpleLogger.setLogger(new AndroidLogger());
        SimpleLogger.d(getClass(), "onCreate()");
        if (clientClass == null) {
            throw new IllegalArgumentException("ClientClass must be set before calling super.onCreate()");
        }
        activity = this;
        super.onCreate(savedInstanceState);
        createCoreWindows(getRenderVersion());
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
     * @param layoutParams
     * @param windowFeature
     */
    private void setup(Renderers version, int rendermode, int layoutParams, int windowFeature) {
        SurfaceConfiguration surfaceConfig = createSurfaceConfig();
        createWrapper(version);
        surfaceConfig.setSamples(getSamples());
        mGLView = new AndroidSurfaceView(surfaceConfig, version, getApplicationContext(), this);
        mGLView.setRenderMode(rendermode);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mGLView);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        com.nucleus.renderer.Window.getInstance().setScreenSize(size.x, size.y);
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

    @Override
    public void createCoreWindows(Renderers version) {
        setup(version, GLSurfaceView.RENDERMODE_CONTINUOUSLY, LayoutParams.FLAG_FULLSCREEN,
                Window.FEATURE_NO_TITLE);
    }

    @Override
    public void createCoreApp(int width, int height) {
        NucleusRenderer renderer = RendererFactory.getRenderer(gles, new AndroidImageFactory(),
                new AndroidMatrixEngine());
        coreApp = CoreApp.createCoreApp(width, height, renderer, clientClass);
        mGLView.setCoreApp(coreApp);
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
}
