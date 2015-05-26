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
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RendererFactory;
import com.nucleus.renderer.RendererFactory.Renderers;
import com.nucleus.texture.android.AndroidImageFactory;

public class BaseActivity extends Activity {
    protected GLSurfaceView mGLView;
    protected PointerInputProcessor inputProcessor = new PointerInputProcessor();
    protected GLES20Wrapper gles = new AndroidGLES20Wrapper();
    protected NucleusRenderer renderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        renderer = RendererFactory.getRenderer(Renderers.GLES20, gles, new AndroidImageFactory(),
                new AndroidMatrixEngine());
        setup(renderer, GLSurfaceView.RENDERMODE_CONTINUOUSLY, LayoutParams.FLAG_FULLSCREEN,
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
    protected void setup(NucleusRenderer renderer, int rendermode, int layoutParams,
            int windowFeature) {
        mGLView = new AndroidSurfaceView(getApplicationContext(), new AndroidRenderer(renderer),
                inputProcessor);
        mGLView.setRenderMode(rendermode);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mGLView);
    }

}
