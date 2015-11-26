package com.nucleus.convolution;

import org.junit.Test;

import com.nucleus.convolution.ConvolutionProgram.VARIABLES;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.ExternalReference;
import com.nucleus.jogl.NucleusApplication;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.FrameListener;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.RenderSettings;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.Node;
import com.nucleus.texturing.Convolution;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureSetup;

public class FGLConvolutionTest extends NucleusApplication implements RenderContextListener, FrameListener,
        MMIEventListener {

    private final static int ITERATIONS = 100;

    private final static float[] kernel1 = new float[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };
    private final static float[] kernel2 = new float[] { -1, -1, -1, -1, 8, -1, -1, -1, -1 };
    private final static float[] kernel3 = new float[] { 0, -1, 0, -1, 5, -1, 0, -1, 0 };
    private final static boolean[] absNormalize = new boolean[] { false, true, false };

    private float[][] kernel = new float[][] { kernel1, kernel2, kernel3 };
    private float[] normalizedKernel = new float[9];

    private float factor = 1f;
    private int kernelIndex = 2;
    Mesh mesh;
    int counter = 0;
    long start = 0;

    public static void main(String[] args) {
        FGLConvolutionTest main = new FGLConvolutionTest();
        main.createCore(Renderers.GLES20);
    }

    @Test
    public void testGLConvolution() throws GLException {
        createCore(Renderers.GLES20);
    }

    @Override
    public void createCore(Renderers version) {
        windowWidth = 1920;
        windowHeight = 1080;
        swapInterval = 0;
        super.createCore(version);
    }

    @Override
    public void contextCreated(int width, int height) {
        super.contextCreated(width, height);
        NucleusRenderer renderer = getRenderer();
        RenderSettings rs = renderer.getRenderSettings();
        rs.setCullFace(GLES20.GL_NONE);
        rs.setDepthFunc(GLES20.GL_NONE);
        renderer.getViewFrustum().setOrthoProjection(-0.5f, 0.5f, 0.5f, -0.5f, 0f, 10f);
        coreApp.getInputProcessor().addMMIListener(this);

        mesh = new Mesh();
        ConvolutionProgram c = new ConvolutionProgram();
        c.createProgram(renderer.getGLES());
        Node node = new Node();
        TextureSetup texSetup = new TextureSetup(new ExternalReference("assets/testimage.jpg"), RESOLUTION.HD, 1);
        Texture2D tex = TextureFactory.createTexture(renderer.getGLES(), renderer.getImageFactory(), texSetup);
        c.buildMesh(mesh, tex, 1f, 1f, 0, kernel[kernelIndex]);
        node.addMesh(mesh);
        renderer.setScene(node);
        renderer.addFrameListener(this);

    }

    private void setTest(int test) {

    }

    @Override
    public void processFrame(float deltaTime) {
        factor += deltaTime * 0.2f;
        if (factor > 2.5) {
            factor = 0.3f;
        }
    }

    @Override
    public void updateGLData() {

        if (start == 0) {
            start = System.currentTimeMillis();
        }
        counter++;
        if (counter > 500) {
            long end = System.currentTimeMillis();
            String fillrateStr = "";
            int size = windowWidth * windowHeight;
            int fillrate = (size * counter) / (int) (end - start);
            fillrateStr = " " + Float.toString(fillrate / 1000) + ", mpixels/s";
            window.setTitle(fillrateStr);
            start = System.currentTimeMillis();
            counter = 0;
        }
        Convolution.normalize(kernel[kernelIndex], normalizedKernel, absNormalize[kernelIndex], factor);
        System.arraycopy(normalizedKernel, 0, mesh.getUniformMatrices(), VARIABLES.uKernel.offset,
                normalizedKernel.length);

    }

    @Override
    public void inputEvent(MMIPointerEvent event) {

        switch (event.getAction()) {
        case ACTIVE:
            kernelIndex++;
            if (kernelIndex >= kernel.length) {
                kernelIndex = 0;
            }
            break;
        case INACTIVE:
        case ZOOM:
        case MOVE:
        }

    }
}
