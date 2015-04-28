package com.nucleus.android;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.nucleus.matrix.android.AndroidMatrixEngine;
import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.BaseRenderer;
import com.nucleus.texture.android.AndroidImageFactory;

public class BaseActivity extends Activity {
    protected GLSurfaceView mGLView;
    protected PointerInputProcessor inputProcessor = new PointerInputProcessor();
    protected GLES20Wrapper gles = new AndroidGLES20Wrapper();
    protected BaseRenderer baseRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseRenderer = new BaseRenderer(gles, new AndroidImageFactory(),
                new AndroidMatrixEngine());
        setup(baseRenderer, GLSurfaceView.RENDERMODE_CONTINUOUSLY, LayoutParams.FLAG_FULLSCREEN,
                Window.FEATURE_NO_TITLE);
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
    protected void setup(BaseRenderer baseRenderer, int rendermode, int layoutParams,
            int windowFeature) {
        mGLView = new AndroidSurfaceView(getApplicationContext(), new AndroidRenderer(baseRenderer),
                inputProcessor);
        mGLView.setRenderMode(rendermode);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mGLView);
    }

}
