package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.bounds.Bounds;
import com.nucleus.common.Type;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.GLTFMeshRenderer;
import com.nucleus.renderer.GLTFNodeRenderer;
import com.nucleus.renderer.MeshRenderer;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.RenderableMesh;
import com.nucleus.shader.GLTFShaderProgram;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.ShaderProgram.ProgramType;
import com.nucleus.texturing.Texture2D.Shading;

/**
 * Node containing a glTF model
 *
 */
public class GLTFNode extends AbstractNode implements RenderableNode<RenderableMesh>, MeshBuilder<RenderableMesh> {

    transient protected static NodeRenderer<GLTFNode> nodeRenderer = new GLTFNodeRenderer();

    private static final String GLTF_NAME = "glTFName";

    @SerializedName(GLTF_NAME)
    private String glTFName;

    transient private GLTF glTF;
    transient ArrayList<RenderableMesh> meshes = new ArrayList<>();
    transient GLES20Wrapper gles;
    transient MeshRenderer<RenderableMesh> meshRenderer = new GLTFMeshRenderer();

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    @Deprecated
    protected GLTFNode() {
    }

    private GLTFNode(RootNode root, Type<Node> type) {
        super(root, type);
    }

    @Override
    public Node createInstance(RootNode root) {
        GLTFNode copy = new GLTFNode(root, NodeTypes.gltfnode);
        copy.set(this);
        return copy;
    }

    /**
     * Copy values into this node from the source, used when new instance is created
     * 
     * @param source
     */
    public void set(GLTFNode source) {
        super.set(source);
        this.glTFName = source.glTFName;
    }

    public String getGLTFName() {
        return glTFName;
    }

    @Override
    public ArrayList<RenderableMesh> getMeshes(ArrayList<RenderableMesh> list) {
        list.addAll(meshes);
        return list;
    }

    public GLTF getGLTF() {
        return glTF;
    }

    @Override
    public void createTransient() {
    }

    @Override
    public void addMesh(RenderableMesh mesh) {
        meshes.add(mesh);
    }

    @Override
    public ShaderProgram getProgram() {
        return program;
    }

    @Override
    public void setProgram(ShaderProgram program) {
        this.program = program;
    }

    @Override
    public MeshBuilder<RenderableMesh> createMeshBuilder(GLES20Wrapper gles, ShapeBuilder shapeBuilder)
            throws IOException {
        this.gles = gles;
        return this;
    }

    @Override
    public MeshRenderer<RenderableMesh> getMeshRenderer() {
        return meshRenderer;
    }

    @Override
    public NodeRenderer<GLTFNode> getNodeRenderer() {
        return nodeRenderer;
    }

    @Override
    public RenderableMesh createInstance() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void create(RenderableNode<RenderableMesh> parent) throws IOException, GLException {
        if (glTFName != null) {
            try {
                glTF = AssetManager.getInstance().getGLTFAsset(getRootNode().getGLTFPath() + glTFName);
                AssetManager.getInstance().loadGLTFAssets(gles, glTF);
                setPass(Pass.ALL);
                setState(State.ON);
                createPrograms(glTF);
            } catch (IOException | GLTFException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates, loads and compiles the needed programs for the primitives
     * 
     * @param glTF
     */
    protected void createPrograms(GLTF glTF) {
        if (glTF.getMeshes() != null) {
            for (Mesh m : glTF.getMeshes()) {
                for (Primitive p : m.getPrimitives()) {
                    GLTFShaderProgram program = createProgram(p);
                    p.setProgram((GLTFShaderProgram) AssetManager.getInstance().getProgram(gles, program));
                }
            }
        }
    }

    /**
     * Creates an instance, not compiled or linked, of the shader program needed to render this primitive.
     * 
     * @param primitive
     * @return
     */
    public GLTFShaderProgram createProgram(Primitive primitive) {
        if (primitive.getMaterial().getPbrMetallicRoughness().getBaseColorTexture() != null) {
            return new GLTFShaderProgram(null, Shading.textured, "gltf", ProgramType.VERTEX_FRAGMENT);
        }
        return new GLTFShaderProgram(null, Shading.flat, "gltf", ProgramType.VERTEX_FRAGMENT);
    }

    @Override
    public RenderableMesh create() throws IOException, GLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bounds createBounds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ShaderProgram createProgram() {
        return null;
    }

}
