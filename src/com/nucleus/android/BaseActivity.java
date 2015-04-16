package com.nucleus.android;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.BaseRenderer;

public class BaseActivity extends Activity {
    protected GLSurfaceView mGLView;
    protected PointerInputProcessor inputProcessor = new PointerInputProcessor();
    protected Renderer renderer;
    protected GLES20Wrapper gles = new AndroidGLES20Wrapper();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Setup this activity with a new GLSurfaceView create with the specified renderer
     * When this method returns the created view is the active content view (ie visible)
     * 
     * @param renderer
     * @param rendermove
     * @param layoutParams
     * @param windowFeature
     */
    protected void setup(BaseRenderer baseRenderer, int rendermove, int layoutParams, int windowFeature) {
        renderer = new AndroidRenderer(baseRenderer);
        mGLView = new AndroidSurfaceView(getApplicationContext(), renderer, inputProcessor);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mGLView);
    }

}
