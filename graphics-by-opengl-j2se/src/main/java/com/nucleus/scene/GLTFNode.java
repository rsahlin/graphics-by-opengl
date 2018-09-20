package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.common.Type;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.GLTFMeshRenderer;
import com.nucleus.renderer.GLTFNodeRenderer;
import com.nucleus.renderer.MeshRenderer;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.RenderableMesh;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.TranslateProgram;
import com.nucleus.texturing.Texture2D.Shading;

/**
 * Node containing a glTF model
 *
 */
public class GLTFNode extends AbstractNode implements RenderableNode<RenderableMesh> {

    transient protected static NodeRenderer<GLTFNode> nodeRenderer = new GLTFNodeRenderer();
    transient protected static MeshRenderer<RenderableMesh> meshRenderer = new GLTFMeshRenderer();

    private static final String GLTF_NAME = "glTFName";

    @SerializedName(GLTF_NAME)
    private String glTFName;

    transient private GLTF glTF;
    transient ArrayList<RenderableMesh> meshes = new ArrayList<>();

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
    public void onCreated() {
        super.onCreated();
        if (glTFName != null) {
            int index = getRootNode().getGLTFIndex(glTFName);
            try {
                glTF = AssetManager.getInstance().loadGLTFAsset(getRootNode().getGLTFPath(), glTFName, index);
                setPass(Pass.ALL);
                setState(State.ON);
            } catch (IOException | GLTFException e) {
                throw new RuntimeException(e);
            }
        }
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
        program = new TranslateProgram(Shading.flat);
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
        return null;
    }

    @Override
    public MeshRenderer<RenderableMesh> getMeshRenderer() {
        return meshRenderer;
    }

    @Override
    public NodeRenderer<GLTFNode> getNodeRenderer() {
        return nodeRenderer;
    }
    
}
