package com.nucleus.scene;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.nucleus.SimpleLogger;
import com.nucleus.assets.AssetManager;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.shape.RectangleShapeBuilder;
import com.nucleus.geometry.shape.RectangleShapeBuilder.RectangleConfiguration;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.Backend.DrawMode;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderPass;
import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.Target;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.AbstractNode.NodeTypes;
import com.nucleus.scene.gltf.GLTFRootNode;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.TranslateProgram;
import com.nucleus.texturing.BaseImageFactory;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.vecmath.Rectangle;

/**
 * Builder for RootNode - will help create RootNode with minimal nodes to support different usecases.
 * 
 *
 */
public class RootNodeBuilder {

    public static final String NUCLEUS_SCENE = "nucleusScene";
    public static final String GLTF_SCENE = "gltfScene";

    private NodeBuilder<Node> nodeBuilder;

    public RootNode create(NucleusRenderer renderer, String id, String type) throws NodeException {
        if (nodeBuilder == null) {
            throw new IllegalArgumentException("Builder is null");
        }
        RootNode root = newInstance(type, id);
        nodeBuilder.setRoot(root);
        nodeBuilder.createRoot(renderer, nodeBuilder.create("scene"));
        return root;
    }

    /**
     * Creates a rootnode for a scene displaying splash image
     * Projection will be set to orthographic with aspect (width / height) with height normalized
     * 
     * @param renderer
     * @param splashImage
     * @param width Width of window
     * @param height Height of window
     * @return
     * @throws NodeException
     */
    public RootNode createSplashRoot(NucleusRenderer renderer, String splashImage, RESOLUTION splashResolution,
            int width,
            int height)
            throws NodeException {
        RootNode root = newInstance(NUCLEUS_SCENE, "splashroot");
        NodeBuilder<Node> builder = new NodeBuilder<>();
        builder.setRoot(root);
        TranslateProgram vt = (TranslateProgram) AssetManager.getInstance().getProgram(renderer.getGLES(),
                new TranslateProgram(ShaderProgram.Shading.textured));
        builder.setProgram(vt);
        TextureParameter texParam = new TextureParameter(TextureParameter.DEFAULT_TEXTURE_PARAMETERS);
        Texture2D texture = AssetManager.getInstance().getTexture(renderer,
                BaseImageFactory.getInstance(), "texture",
                new ExternalReference(splashImage), splashResolution, texParam, 1);
        Mesh.Builder<Mesh> meshBuilder = new Mesh.Builder<>(renderer);
        meshBuilder.setElementMode(DrawMode.TRIANGLES, 4, 0, 6);
        meshBuilder.setTexture(texture);
        Material material = new Material();
        Rectangle rect = texture.calculateRectangle(0);
        meshBuilder.setMaterial(material).setAttributesPerVertex(vt.getAttributeSizes())
                .setShapeBuilder(new RectangleShapeBuilder(new RectangleConfiguration(rect, 1f, 1, 0)));
        builder.setType(NodeTypes.meshnode).setMeshBuilder(meshBuilder).setMeshCount(1);
        RenderPass pass = new RenderPass("RenderPass", new RenderTarget(Target.FRAMEBUFFER, null), new RenderState(),
                Pass.MAIN);
        ArrayList<RenderPass> passes = new ArrayList<>();
        passes.add(pass);
        builder.setRenderPass(passes);
        ViewFrustum vf = new ViewFrustum();
        float aspect = (float) width / height;
        vf.setOrthoProjection(-aspect / 2, aspect / 2, -0.5f, 0.5f, 0, 10);
        builder.setViewFrustum(vf);
        return builder.createRoot(renderer, builder.create("scene"));
    }

    /**
     * Sets the NodeBuilder, this will be used to create child nodes when {@link #create(GLES20Wrapper, String, String)}
     * method is called.
     * 
     * @param nodeBuilder
     * @return
     */
    public RootNodeBuilder setNodeBuilder(NodeBuilder<Node> nodeBuilder) {
        this.nodeBuilder = nodeBuilder;
        return this;
    }

    /**
     * Utility method to get the classOffT for the file type - use this when scene is loaded.
     * Currently supports {@value #NUCLEUS_SCENE} and {@value #GLTF_SCENE}
     * 
     * @param fileType
     * @return
     * @throws IllegalArgumentException If type is not a valid value
     */
    public static Type getTypeClass(String fileType) {
        if (fileType.contentEquals(NUCLEUS_SCENE)) {
            return RootNodeImpl.class;
        }
        if (fileType.contentEquals(GLTF_SCENE)) {
            return GLTFRootNode.class;
        }
        throw new IllegalArgumentException("Invalid file/scene type: " + fileType);
    }

    /**
     * Creates a new instance of the rootnode for the scene/file type, use this when scene is created programmatically.
     * 
     * @param type
     * @return The created node or null
     */
    protected RootNode newInstance(String type, String id) {
        try {
            RootNode root = (RootNode) Class.forName(getTypeClass(type).getTypeName()).newInstance();
            root.setId(id);
            return root;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            SimpleLogger.d(getClass(), e.toString());
        }
        return null;
    }

}
