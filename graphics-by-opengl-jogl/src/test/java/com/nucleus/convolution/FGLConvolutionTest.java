package com.nucleus.convolution;

import java.nio.FloatBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.nucleus.Backend.DrawMode;
import com.nucleus.BackendException;
import com.nucleus.CoreApp;
import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.common.Type;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.RectangleShapeBuilder;
import com.nucleus.io.ExternalReference;
import com.nucleus.jogl.JOGLApplication;
import com.nucleus.mmi.MMIPointer;
import com.nucleus.mmi.MMIPointerInput;
import com.nucleus.mmi.core.CoreInput;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.geometry.GLMesh;
import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.renderer.BaseRenderer.FrameListener;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeBuilder;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.RootNodeBuilder;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.texturing.BaseImageFactory;
import com.nucleus.texturing.Convolution;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.Texture2D.Format;

import junit.framework.Assert;

public class FGLConvolutionTest extends JOGLApplication implements FrameListener,
        MMIPointerInput {

    
    private Semaphore updateCalls = new Semaphore(0);
    
    /**
     * The types that can be used to represent classes when importing/exporting
     * This is used as a means to decouple serialized name from implementing class.
     * 
     */
    public enum ClientClasses implements Type<Object> {
            clientclass(MyClientApplication.class);

        private final Class<?> theClass;

        private ClientClasses(Class<?> theClass) {
            this.theClass = theClass;
        }

        @Override
        public Class<Object> getTypeClass() {
            return (Class<Object>) theClass;
        }

        @Override
        public String getName() {
            return name();
        }
    }

    public static class MyClientApplication implements ClientApplication {

        @Override
        public void init(CoreApp coreApp) {

        }

        @Override
        public void beginFrame(float deltaTime) {
            // TODO Auto-generated method stub

        }

        @Override
        public void endFrame(float deltaTime) {
            // TODO Auto-generated method stub

        }

        @Override
        public String getAppName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getVersion() {
            // TODO Auto-generated method stub
            return null;
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
    int counter = 0;
    long start = 0;
    private NamedShaderVariable uKernel;
    private GraphicsShader program;

    /**
     * Empty constructor for test and main
     */
    public FGLConvolutionTest() {
        super(new String[] {}, Renderers.GLES30, ClientClasses.clientclass);
    }

    public static void main(String[] args) {
        FGLConvolutionTest main = new FGLConvolutionTest();
    }

    /**
     * When running as unit test the classloader points to wrong folder
     */
//    @Test
    public void testGLConvolution() throws GLException {
        
        try {
            Assert.assertEquals(updateCalls.tryAcquire(100, 2000000, TimeUnit.MILLISECONDS), true);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        tearDown();
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
        CoreInput.getInstance().addMMIListener(this);

        try {
            RootNodeBuilder rootBuilder = new RootNodeBuilder();
            NodeBuilder<Node> builder = new NodeBuilder<>();
            TextureParameter texParam = new TextureParameter(TextureParameter.DEFAULT_TEXTURE_PARAMETERS);
            Texture2D texture = renderer.getAssets().getTexture(renderer,
                    BaseImageFactory.getInstance(), "texture",
                    new ExternalReference("assets/testimage.jpg"), Format.RGB, RESOLUTION.HD, texParam, 1);
            MeshBuilder<Mesh> meshBuilder = new GLMesh.Builder<>(renderer);
            meshBuilder.setElementMode(DrawMode.TRIANGLES, 4, 0, 6);
            meshBuilder.setTexture(texture);
            try {
                program = renderer.getAssets().getGraphicsPipeline(renderer, new ConvolutionProgram());
            } catch (BackendException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            Material material = new Material();
            meshBuilder.setMaterial(material).setAttributesPerVertex(program.getPipeline().getAttributeSizes());
            meshBuilder.setShapeBuilder(
                    new RectangleShapeBuilder(new RectangleShapeBuilder.RectangleConfiguration(1f, 1f, 0f, 1, 0)));
            builder.setType(com.nucleus.scene.AbstractNode.NodeTypes.layernode).setMeshBuilder(meshBuilder)
                    .setMeshCount(1);
            rootBuilder.setNodeBuilder(builder);
            RootNode root = rootBuilder.create(renderer, "rootnode", RootNodeBuilder.NUCLEUS_SCENE);
            uKernel = program.getPipeline().getUniformByName("uKernel");
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
        if (counter > 100) {
            long end = System.currentTimeMillis();
            String fillrateStr = "";
            int size = windowWidth * windowHeight;
            int fillrate = (size * counter) / (int) (end - start);
            fillrateStr = " " + Float.toString(fillrate / 1000) + ", mpixels/s";
            start = System.currentTimeMillis();
            counter = 0;
        }
        Convolution.normalize(kernel[kernelIndex], normalizedKernel, absNormalize[kernelIndex], factor);
        FloatBuffer fb = program.getUniformData();
        fb.position(uKernel.getOffset());
        fb.put(normalizedKernel, 0, normalizedKernel.length);
        updateCalls.release();
    }

    @Override
    public void onInput(MMIPointer event) {

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
