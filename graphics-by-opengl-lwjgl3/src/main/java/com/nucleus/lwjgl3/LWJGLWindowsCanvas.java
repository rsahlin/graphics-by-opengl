package com.nucleus.lwjgl3;

import static org.lwjgl.system.windows.GDI32.ChoosePixelFormat;
import static org.lwjgl.system.windows.GDI32.DescribePixelFormat;
import static org.lwjgl.system.windows.GDI32.GetPixelFormat;
import static org.lwjgl.system.windows.GDI32.PFD_DOUBLEBUFFER;
import static org.lwjgl.system.windows.GDI32.PFD_DRAW_TO_WINDOW;
import static org.lwjgl.system.windows.GDI32.PFD_MAIN_PLANE;
import static org.lwjgl.system.windows.GDI32.PFD_TYPE_RGBA;
import static org.lwjgl.system.windows.GDI32.SetPixelFormat;

import org.lwjgl.opengl.WGL;
import org.lwjgl.system.jawt.JAWTDrawingSurfaceInfo;
import org.lwjgl.system.jawt.JAWTWin32DrawingSurfaceInfo;
import org.lwjgl.system.windows.GDI32;
import org.lwjgl.system.windows.PIXELFORMATDESCRIPTOR;
import org.lwjgl.system.windows.WinBase;

import com.nucleus.J2SEWindow;
import com.nucleus.SimpleLogger;

@SuppressWarnings("serial")
public class LWJGLWindowsCanvas extends LWJGLCanvas {

    public LWJGLWindowsCanvas(J2SEWindow window, int width, int height) {
        super(window, width, height);
        SimpleLogger.d(getClass(), "Using Windows Canvas");
    }

    @Override
    protected void createContext(long hdc) {
        try (
                /**
                 * TODO - use SurfaceConfiguration to choose
                 */
                PIXELFORMATDESCRIPTOR pfd = PIXELFORMATDESCRIPTOR.calloc()
                        .nSize((byte) PIXELFORMATDESCRIPTOR.SIZEOF)
                        .nVersion((short) 1)
                        .dwFlags(GDI32.PFD_SUPPORT_OPENGL | PFD_DRAW_TO_WINDOW | PFD_DOUBLEBUFFER)
                        .iPixelType(PFD_TYPE_RGBA)
                        .cColorBits((byte) 32)
                        .cAlphaBits((byte) 8)
                        .cDepthBits((byte) 24)
                        .iLayerType(PFD_MAIN_PLANE)) {
            int pixelFormat = GetPixelFormat(hdc);
            if (pixelFormat != 0) {
                if (DescribePixelFormat(hdc, pixelFormat, pfd) == 0) {
                    throw new IllegalStateException("DescribePixelFormat() failed: " + WinBase.getLastError());
                }
            } else {
                pixelFormat = ChoosePixelFormat(hdc, pfd);
                if (pixelFormat < 1) {
                    throw new IllegalStateException("ChoosePixelFormat() failed: " + WinBase.getLastError());
                }

                if (!SetPixelFormat(hdc, pixelFormat, null)) {
                    throw new IllegalStateException("SetPixelFormat() failed: " + WinBase.getLastError());
                }
            }
        }

        context = WGL.wglCreateContext(hdc);
    }

    @Override
    protected long getHDC(JAWTDrawingSurfaceInfo dsi) {
        // Get the platform-specific drawing info
        JAWTWin32DrawingSurfaceInfo dsi_win = JAWTWin32DrawingSurfaceInfo.create(dsi.platformInfo());
        return dsi_win.hdc();
    }

    @Override
    protected void swapBuffers(long hdc) {
        GDI32.SwapBuffers(hdc);
    }

    @Override
    protected boolean makeCurrent(long hdc) {
        return WGL.wglMakeCurrent(hdc, context);

    }

    @Override
    protected void deleteContext(long context) {
        WGL.wglDeleteContext(context);

    }

}
