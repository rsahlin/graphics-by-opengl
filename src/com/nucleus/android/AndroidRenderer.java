package com.nucleus.android;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.renderer.BaseRenderer;
import com.nucleus.texturing.Image;
import com.nucleus.texturing.Image.ImageFormat;
import com.nucleus.texturing.ImageFactory;

/**
 * Base implementation for Android renderer used with GLSurfaceView
 * This will handle the most common situations for rendering and reading input events.
 * 
 * @author Richard Sahlin
 */
public abstract class AndroidRenderer extends BaseRenderer implements Renderer, ImageFactory {

    public final static String ANDROID_RENDERER_TAG = "AndroidRenderer";

    public AndroidRenderer() {
        super(new AndroidGLES20Wrapper());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        viewFrustum.setViewPort(0, 0, width, height);
        viewFrustum.setOrthoProjection(0, 1, 1, 0, 0, -10);
        if (contextCreated) {
            GLContextCreated(width, height);
            contextCreated = false;
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        beginFrame();
        render();
        endFrame();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        init();
        if (width == 0) {
            contextCreated = true;
        } else {
            GLContextCreated(width, height);
        }
    }

    @Override
    public void setProjectionMatrix(ViewFrustum viewFrustum) {

        float[] projection = viewFrustum.getProjection();
        switch (viewFrustum.getProjectionType()) {
        case ViewFrustum.PROJECTION_ORTHOGONAL:
            Matrix.orthoM(viewFrustum.getProjectionMatrix(), 0, projection[ViewFrustum.LEFT_INDEX],
                    projection[ViewFrustum.RIGHT_INDEX], projection[ViewFrustum.BOTTOM_INDEX],
                    projection[ViewFrustum.TOP_INDEX], projection[ViewFrustum.NEAR_INDEX],
                    projection[ViewFrustum.FAR_INDEX]);
        }

    }

    @Override
    public Image createImage(String name, float scaleX, float scaleY) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        Bitmap b = BitmapFactory.decodeStream(classLoader.getResourceAsStream(name));
        if (b == null) {
            throw new IOException("Could not load " + name);
        }
        if (scaleX != 1 || scaleY != 1) {
            Bitmap copy = Bitmap.createScaledBitmap(b, (int) (b.getWidth() * scaleX), (int) (b.getHeight() * scaleY),
                    true);
            b = copy;
        }

        Image image = new Image(b.getWidth(), b.getHeight(), ImageFormat.RGBA);
        b.copyPixelsToBuffer(image.getBuffer().position(0));
        return image;
    }
}
