package com.nucleus.convolution;

import org.junit.Test;

import com.nucleus.CoreApp;
import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.assets.AssetManager;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.Mode;
import com.nucleus.geometry.RectangleShapeBuilder;
import com.nucleus.io.ExternalReference;
import com.nucleus.jogl.JOGLApplication;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.FrameListener;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.BaseRootNode;
import com.nucleus.scene.DefaultNodeFactory;
import com.nucleus.scene.Node.MeshIndex;
import com.nucleus.scene.Node.NodeTypes;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.RootNode;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.texturing.Convolution;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureParameter;

public class FGLConvolutionTest extends JOGLApplication implements FrameListener,
        MMIEventListener {

    public static class MyClientApplication implements ClientApplication {

        @Override
        public void init(CoreApp coreApp) {

        }

    }

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
    private ShaderVariable uKernel;
    private ConvolutionProgram program;

    public FGLConvolutionTest() {
        super(new String[] {}, Renderers.GLES20, MyClientApplication.class);
    }

    public static void main(String[] args) {
        FGLConvolutionTest main = new FGLConvolutionTest();
    }

    @Test
    public void testGLConvolution() throws GLException {
        createCoreWindows(Renderers.GLES20);
    }

    @Override
    public void createCoreWindows(Renderers version) {
        windowWidth = 1920;
        windowHeight = 1080;
        swapInterval = 0;
        super.createCoreWindows(version);
    }

    @Override
    public void createCoreApp(int width, int height) {
        super.createCoreApp(width, height);
        NucleusRenderer renderer = coreApp.getRenderer();
        coreApp.getInputProcessor().addMMIListener(this);

        BaseRootNode.Builder builder = new BaseRootNode.Builder(renderer);
        TextureParameter texParam = new TextureParameter(TextureParameter.DEFAULT_TEXTURE_PARAMETERS);
        Texture2D texture = TextureFactory.createTexture(renderer.getGLES(), renderer.getImageFactory(), "texture",
                new ExternalReference("assets/testimage.jpg"), RESOLUTION.HD, texParam, 1);
        Mesh.Builder<Mesh> meshBuilder = new Mesh.Builder<>(renderer);
        meshBuilder.setElementMode(Mode.TRIANGLES, 4, 6);
        meshBuilder.setTexture(texture);
        program = (ConvolutionProgram) AssetManager.getInstance().getProgram(renderer.getGLES(),
                new ConvolutionProgram());
        Material material = new Material();
        material.setProgram(program);
        meshBuilder.setMaterial(material);
        meshBuilder.setShapeBuilder(
                new RectangleShapeBuilder(new RectangleShapeBuilder.RectangleConfiguration(1f, 1f, 0f, 1, 0)));
        builder.setMeshBuilder(meshBuilder).setNodeFactory(new DefaultNodeFactory())
                .setNode(NodeTypes.layernode);
        try {
            RootNode root = builder.create();
            mesh = root.getNodeByType(NodeTypes.layernode).getMesh(MeshIndex.MAIN);
            uKernel = program.getShaderVariable(ConvolutionProgram.VARIABLES.uKernel);
            renderer.addFrameListener(this);
            coreApp.setRootNode(root);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }

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
            start = System.currentTimeMillis();
            counter = 0;
        }
        Convolution.normalize(kernel[kernelIndex], normalizedKernel, absNormalize[kernelIndex], factor);
        System.arraycopy(normalizedKernel, 0, program.getUniformData(), uKernel.getOffset(),
                normalizedKernel.length);

    }

    @Override
    public void onInputEvent(MMIPointerEvent event) {

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
