package com.nucleus.android;

import com.nucleus.CoreApp;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.SimpleLogger;
import com.nucleus.matrix.android.AndroidMatrixEngine;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.RendererFactory;
import com.nucleus.renderer.SurfaceConfiguration;
import com.nucleus.texture.android.AndroidImageFactory;
import com.super2k.nucleus.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * Base activity to get NucleusRenderer functionality on Android.
 * 
 * @author Richard Sahlin
 *
 */
public class NucleusActivity extends Activity implements DialogInterface.OnClickListener, CoreAppStarter {

    /**
     * Android specific objects
     */
    protected GLSurfaceView mGLView;
    private static Throwable throwable;
    private static NucleusActivity activity;

    protected CoreApp coreApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SimpleLogger.setLogger(new AndroidLogger());
        SimpleLogger.d(getClass(), "onCreate()");
        activity = this;
        super.onCreate(savedInstanceState);
        createCore(Renderers.GLES20);
    }

    @Override
    public void onResume() {
        SimpleLogger.d(getClass(), "onResume()");
        super.onResume();
    }

    /**
     * Setup this activity with a new GLSurfaceView create with the specified renderer
     * When this method returns the created view is the active content view (ie visible)
     * 
     * @param rendermode
     * @param layoutParams
     * @param windowFeature
     */
    private void setup(int rendermode, int layoutParams, int windowFeature) {
        SurfaceConfiguration surfaceConfig = new SurfaceConfiguration();
        // TODO This shall be set on a per project basis
        surfaceConfig.setSamples(16);
        mGLView = new AndroidSurfaceView(coreApp, surfaceConfig, getApplicationContext(), coreApp.getInputProcessor());
        mGLView.setRenderMode(rendermode);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mGLView);
    }

    public static void handleThrowable(final Throwable t) {
        throwable = t;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.showAlert(activity.getString(R.string.app_name) + " " + activity.getString(R.string.crash),
                        t.toString());
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
    public void createCore(Renderers version) {
        if (coreApp == null) {
            GLES20Wrapper gles = new AndroidGLES20Wrapper();
            coreApp = new CoreApp(RendererFactory.getRenderer(gles, new AndroidImageFactory(),
                    new AndroidMatrixEngine()));
            setup(GLSurfaceView.RENDERMODE_CONTINUOUSLY, LayoutParams.FLAG_FULLSCREEN,
                    Window.FEATURE_NO_TITLE);
        }
    }

}
