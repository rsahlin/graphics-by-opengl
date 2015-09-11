package com.nucleus.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.nucleus.matrix.android.AndroidMatrixEngine;
import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RendererFactory;
import com.nucleus.renderer.RendererFactory.Renderers;
import com.nucleus.texture.android.AndroidImageFactory;
import com.super2k.nucleus.android.R;

public class BaseActivity extends Activity implements DialogInterface.OnClickListener {
    protected GLSurfaceView mGLView;
    protected PointerInputProcessor inputProcessor = new PointerInputProcessor();
    protected GLES20Wrapper gles = new AndroidGLES20Wrapper();
    protected NucleusRenderer renderer;
    private AndroidRenderer androidRenderer;

    private static Throwable throwable;
    private static BaseActivity activity;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	activity = this;
    	try {
            super.onCreate(savedInstanceState);
            renderer = RendererFactory.getRenderer(Renderers.GLES20, gles, new AndroidImageFactory(),
                    new AndroidMatrixEngine());
            setup(renderer, GLSurfaceView.RENDERMODE_CONTINUOUSLY, LayoutParams.FLAG_FULLSCREEN,
                    Window.FEATURE_NO_TITLE);
    	} catch (Throwable t) {
    		throwable = t;
    	}
    }

    /**
     * Setup this activity with a new GLSurfaceView create with the specified renderer
     * When this method returns the created view is the active content view (ie visible)
     * 
     * @param renderer
     * @param appListener
     * @param rendermode
     * @param layoutParams
     * @param windowFeature
     */
    protected void setup(NucleusRenderer renderer, int rendermode, int layoutParams,
            int windowFeature) {
    	androidRenderer = new AndroidRenderer(renderer);
        mGLView = new AndroidSurfaceView(getApplicationContext(), androidRenderer, inputProcessor);
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
		    	activity.showAlert(activity.getString(R.string.app_name) + " " + activity.getString(R.string.crash), t.toString());
			}
		});
    }
    
    /**
     * Will display alert message with neutral button to dismiss.
     * Use this in case of unrecoverable error, for instance server error.
     * MUST be called from the UI thread!
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
    
    
}
