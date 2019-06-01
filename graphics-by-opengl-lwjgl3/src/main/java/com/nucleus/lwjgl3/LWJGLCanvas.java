
/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package com.nucleus.lwjgl3;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.jawt.JAWTFunctions.JAWT_DrawingSurface_FreeDrawingSurfaceInfo;
import static org.lwjgl.system.jawt.JAWTFunctions.JAWT_DrawingSurface_GetDrawingSurfaceInfo;
import static org.lwjgl.system.jawt.JAWTFunctions.JAWT_DrawingSurface_Lock;
import static org.lwjgl.system.jawt.JAWTFunctions.JAWT_DrawingSurface_Unlock;
import static org.lwjgl.system.jawt.JAWTFunctions.JAWT_GetAWT;
import static org.lwjgl.system.jawt.JAWTFunctions.JAWT_GetDrawingSurface;
import static org.lwjgl.system.jawt.JAWTFunctions.JAWT_LOCK_ERROR;
import static org.lwjgl.system.jawt.JAWTFunctions.JAWT_VERSION_1_4;

import java.awt.Canvas;
import java.awt.Graphics;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.jawt.JAWT;
import org.lwjgl.system.jawt.JAWTDrawingSurface;
import org.lwjgl.system.jawt.JAWTDrawingSurfaceInfo;

import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.SimpleLogger;
import com.nucleus.renderer.SurfaceConfiguration;

/**
 * A Canvas component that uses OpenGL for rendering.
 * This is used when an AWT window is the target
 */
@SuppressWarnings("serial")
public abstract class LWJGLCanvas extends Canvas {

    protected final JAWT awt;
    protected long context;

    protected GLESCapabilities caps;
    J2SEWindow window;
    CoreApp coreApp;
    SurfaceConfiguration config;
    int width;
    int height;

    public LWJGLCanvas(J2SEWindow window, SurfaceConfiguration config, int width, int height) {
        this.config = config;
        awt = JAWT.calloc();
        awt.version(JAWT_VERSION_1_4);
        if (!JAWT_GetAWT(awt)) {
            throw new RuntimeException("GetAWT failed");
        }
        if (window == null) {
            throw new IllegalArgumentException("Window is null");
        }
        this.window = window;
        this.width = width;
        this.height = height;
        setSize(width, height);
    }

    protected abstract long getHDC(JAWTDrawingSurfaceInfo dsi);

    protected abstract void createContext(long hdc);

    protected abstract void swapBuffers(long hdc);

    protected abstract boolean makeCurrent(long hdc);

    protected abstract void deleteContext(long context);

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        // Get the drawing surface
        JAWTDrawingSurface ds = JAWT_GetDrawingSurface(awt.GetDrawingSurface(), this);
        // JAWTDrawingSurface ds = JAWT_GetDrawingSurface(this, awt.GetDrawingSurface());
        if (ds == null) {
            throw new RuntimeException("awt->GetDrawingSurface() failed");
        }
        // Lock the drawing surface
        int lock = JAWT_DrawingSurface_Lock(ds.Lock(), ds);
        // int lock = JAWT_DrawingSurface_Lock(ds, ds.Lock());
        if ((lock & JAWT_LOCK_ERROR) != 0) {
            throw new RuntimeException("ds->Lock() failed");
        }

        try {
            // Get the drawing surface info
            JAWTDrawingSurfaceInfo dsi = JAWT_DrawingSurface_GetDrawingSurfaceInfo(ds.GetDrawingSurfaceInfo(), ds);
            // JAWTDrawingSurfaceInfo dsi = JAWT_DrawingSurface_GetDrawingSurfaceInfo(ds, ds.GetDrawingSurfaceInfo());
            try {
                long hdc = getHDC(dsi);
                if (hdc != NULL) {
                    if (context == NULL) {
                        try {
                            createContext(hdc);
                            if (context == NULL) {
                                throw new IllegalStateException("createContext() failed");
                            }
                            if (!makeCurrent(hdc)) {
                                throw new IllegalStateException("makeCurrent() failed");
                            }
                            if (caps == null) {
                                // Bypasses the default create() method.
                                Configuration.OPENGLES_EXPLICIT_INIT.set(true);
                                GLES.create(GL.getFunctionProvider());
                                caps = GLES.createCapabilities();
                                window.internalCreateCoreApp(width, height);
                                swapBuffers(hdc);
                                window.internalContextCreated(width, height);
                            }
                        } catch (Throwable t) {
                            this.setVisible(false);
                            throw new IllegalArgumentException("Could not create context:", t);
                        }
                    } else {
                        if (!makeCurrent(hdc)) {
                            throw new IllegalStateException("makeCurrent() failed");
                        }
                        GLES.setCapabilities(caps);
                    }
                    if (coreApp != null) {
                        coreApp.renderFrame();
                        swapBuffers(hdc);
                    } else {
                        SimpleLogger.d(getClass(), "CoreApp is null");
                    }
                }
            } finally {
                // Free the drawing surface info
                JAWT_DrawingSurface_FreeDrawingSurfaceInfo(ds.FreeDrawingSurfaceInfo(), dsi);
                // JAWT_DrawingSurface_FreeDrawingSurfaceInfo(dsi, ds.FreeDrawingSurfaceInfo());
            }
        } finally {
            // Unlock the drawing surface
            JAWT_DrawingSurface_Unlock(ds.Unlock(), ds);
            // JAWT_DrawingSurface_Unlock(ds, ds.Unlock());
        }
        repaint();
    }

    public void destroy() {
        awt.free();
        if (context != NULL) {
            deleteContext(context);
        }
    }

    protected void setCoreApp(CoreApp coreApp) {
        this.coreApp = coreApp;
    }

}