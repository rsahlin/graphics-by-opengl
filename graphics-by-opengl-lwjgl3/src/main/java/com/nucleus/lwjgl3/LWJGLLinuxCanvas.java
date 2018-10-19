package com.nucleus.lwjgl3;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GLX;
import org.lwjgl.opengl.GLX13;
import org.lwjgl.system.jawt.JAWTDrawingSurfaceInfo;
import org.lwjgl.system.jawt.JAWTX11DrawingSurfaceInfo;
import org.lwjgl.system.linux.X11;

import com.nucleus.J2SEWindow;
import com.nucleus.SimpleLogger;
import com.nucleus.renderer.SurfaceConfiguration;

@SuppressWarnings("serial")
/**
 * 
 * The canvas used by JAWTWindow on linux platforms - ie when LWJGL shall be used with an AWT window (on linux)
 * Currently this uses GLX13 setup
 * Migrate to GLX14 to get support for more advanced features such as samples
 *
 */
public class LWJGLLinuxCanvas extends LWJGLCanvas {

    private JAWTX11DrawingSurfaceInfo X11Dsi;

    public LWJGLLinuxCanvas(J2SEWindow window, SurfaceConfiguration config, int width, int height) {
        super(window, config, width, height);
        SimpleLogger.d(getClass(), "Using Linux Canvas");
    }

    @Override
    protected void createContext(long hdc) {

        /**
         * TODO - use SurfaceConfiguration to choose
         */
        int screen = X11.XDefaultScreen(hdc);
        IntBuffer attrib_list = BufferUtils.createIntBuffer(16 * 2);
        attrib_list.put(GLX13.GLX_DRAWABLE_TYPE).put(GLX13.GLX_WINDOW_BIT);
        attrib_list.put(GLX13.GLX_RENDER_TYPE).put(GLX13.GLX_RGBA_BIT);
        attrib_list.put(GLX.GLX_RED_SIZE).put(config.getRedBits());
        attrib_list.put(GLX.GLX_GREEN_SIZE).put(config.getGreenBits());
        attrib_list.put(GLX.GLX_BLUE_SIZE).put(config.getBlueBits());
        attrib_list.put(GLX.GLX_DEPTH_SIZE).put(config.getDepthBits());
        attrib_list.put(GLX.GLX_DOUBLEBUFFER).put(1);
        attrib_list.put(0);
        attrib_list.flip();
        PointerBuffer fbConfigs = GLX13.glXChooseFBConfig(hdc, screen, attrib_list);
        if (fbConfigs == null || fbConfigs.capacity() == 0) {
            // No framebuffer configurations supported!
            throw new IllegalArgumentException("No supported framebuffer configurations found");
        }
        context = GLX13.glXCreateNewContext(hdc, fbConfigs.get(0), GLX13.GLX_RGBA_TYPE, NULL, true);

    }

    @Override
    protected long getHDC(JAWTDrawingSurfaceInfo dsi) {
        // Get the platform-specific drawing info
        X11Dsi = JAWTX11DrawingSurfaceInfo.create(dsi.platformInfo());
        return X11Dsi.display();
    }

    @Override
    protected void swapBuffers(long hdc) {
        GLX.glXSwapBuffers(hdc, X11Dsi.drawable());
    }

    @Override
    protected boolean makeCurrent(long hdc) {
        return GLX.glXMakeCurrent(hdc, X11Dsi.drawable(), context);
    }

    @Override
    protected void deleteContext(long context) {
        if (X11Dsi != null) {
            GLX.glXDestroyContext(X11Dsi.display(), context);
        }
    }

}
