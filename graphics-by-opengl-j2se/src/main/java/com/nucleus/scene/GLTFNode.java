package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.SimpleLogger;
import com.nucleus.assets.AssetManager;
import com.nucleus.bounds.Bounds;
import com.nucleus.common.Type;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.GLTFNodeRenderer;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.RenderableMesh;
import com.nucleus.shader.GLTFShaderProgram;
import com.nucleus.shader.GLTFShaderProgram.PBRShading;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.vecmath.Matrix;

/**
 * Node containing a glTF model
 * This is for usecases where the scene is loaded in nucleus scene format and shall contain Nodes that
 * are gltf assets.
 * This is NOT for pure gltf scenes where the complete nodetree is from gltf.
 *
 */
public class GLTFNode extends AbstractMeshNode<RenderableMesh> implements MeshBuilder<RenderableMesh> {

    transient protected static NodeRenderer<GLTFNode> nodeRenderer = new GLTFNodeRenderer();

    /**
     * If this node shall have a preloaded gltf asset
     */
    private static final String GLTF_NAME = "glTFName";

    @SerializedName(GLTF_NAME)
    private String glTFName;

    transient private GLTF glTF;
    transient GLES20Wrapper gles;
    /**
     * Used to save viewmatrix between frames
     */
    transient protected float[] saveViewMatrix = Matrix.setIdentity(Matrix.createMatrix(), 0);

    /**
     * Used by GSON and {@link #createInstance(RootNodeImpl)} method - do NOT call directly
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
     * Returns the name of the glTF asset
     * 
     * @return
     */
    public String getGLTFName() {
        return glTFName;
    }

    /**
     * Loads a gltf asset into this node.
     * 
     * @param gles
     * @paramn glTFName name of gltf asset to load (minus GLTF_PATH)
     * @throws IOException
     * @throws GLException
     */
    public void loadGLTFAsset(GLES20Wrapper gles, String glTFName)
            throws IOException, GLException {
        if (glTFName != null) {
            try {
                glTF = AssetManager.getInstance()
                        .getGLTFAsset(getRootNode().getProperty(RootNodeImpl.GLTF_PATH, "") + glTFName);
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
     * Destroys the gltf asset in this node.
     * If the gltf asset is loaded, all resource are released - buffers and textures but not programs.
     * The resources are destroyed immediately, hence it is important not to call while rendering is taking place.
     * 
     * @param gles
     * @param glTFName
     * @throws GLException
     */
    public void deleteAsset(GLES20Wrapper gles) throws GLException {
        if (glTF != null) {

            AssetManager.getInstance().deleteGLTFAssets(gles, glTF);
            glTF = null;
            glTFName = null;
        } else {
            SimpleLogger.d(getClass(), "No gltf asset in node.");
        }

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

    @Override
    public ArrayList<RenderableMesh> getMeshes(ArrayList<RenderableMesh> list) {
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
        // Since this node implements MeshBuilder the parent will be this class
        if (glTFName != null) {
            loadGLTFAsset(gles, glTFName);
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
     * If no basecolor texture is used the shading will be flat
     * 
     * @param primitive
     * @return
     */
    public GLTFShaderProgram createProgram(Primitive primitive) {
        PBRShading pbrShading = new PBRShading(primitive);
        return new GLTFShaderProgram(pbrShading);
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

    /**
     * Copies the saved viewmatrix to destination matrix
     * 
     * @param matrix
     */
    public void getSavedViewMatrix(float[] matrix) {
        Matrix.copy(saveViewMatrix, 0, matrix, 0);
    }

    /**
     * Copies the viewMatrix to save view matrix in this class.
     * It can then be fetched by calling {@link #getSavedViewMatrix(float[])}
     * 
     * @param viewMatrix
     */
    public void saveViewMatrix(float[] viewMatrix) {
        Matrix.copy(viewMatrix, 0, saveViewMatrix, 0);
    }

}
