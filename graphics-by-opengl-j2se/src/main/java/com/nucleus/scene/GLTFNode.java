package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.Backend.DrawMode;
import com.nucleus.BackendException;
import com.nucleus.SimpleLogger;
import com.nucleus.bounds.Bounds;
import com.nucleus.common.Type;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLTFNodeRenderer;
import com.nucleus.opengl.shader.GLTFShaderProgram;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.Material.ShadingMaps;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.RenderableMesh;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.shader.Shader;
import com.nucleus.texturing.Texture2D;
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
    transient NucleusRenderer renderer;
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
     * @param renderer
     * @paramn glTFName name of gltf asset to load (minus GLTF_PATH)
     * @throws IOException
     * @throws BackendException
     */
    public void loadGLTFAsset(NucleusRenderer renderer, String glTFName)
            throws IOException, BackendException {
        if (glTFName != null) {
            try {
                glTF = renderer.getAssets()
                        .getGLTFAsset(getRootNode().getProperty(RootNodeImpl.GLTF_PATH, "") + glTFName);
                renderer.getAssets().loadGLTFAssets(renderer, glTF);
                setPass(Pass.ALL);
                setState(State.ON);
                createPrograms(glTF);
            } catch (IOException | GLTFException | BackendException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Destroys the gltf asset in this node.
     * If the gltf asset is loaded, all resource are released - buffers and textures but not programs.
     * The resources are destroyed immediately, hence it is important not to call while rendering is taking place.
     * 
     * @param renderer
     * @param glTFName
     * @throws BackendException
     */
    public void deleteAsset(NucleusRenderer renderer) throws BackendException {
        if (glTF != null) {

            renderer.getAssets().deleteGLTFAssets(renderer, glTF);
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
    public MeshBuilder<RenderableMesh> createMeshBuilder(NucleusRenderer renderer, ShapeBuilder shapeBuilder)
            throws IOException {
        this.renderer = renderer;
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
    public void create(RenderableNode<RenderableMesh> parent) throws IOException, BackendException {
        // Since this node implements MeshBuilder the parent will be this class
        if (glTFName != null) {
            loadGLTFAsset(renderer, glTFName);
        }
    }

    /**
     * Creates, loads and compiles the needed programs for the primitives
     * 
     * @param glTF
     * @throws BackendException
     */
    protected void createPrograms(GLTF glTF) throws BackendException {
        if (glTF.getMeshes() != null) {
            for (Mesh m : glTF.getMeshes()) {
                for (Primitive p : m.getPrimitives()) {
                    GLTFShaderProgram program = createProgram(p);
                    renderer.getAssets().getGraphicsPipeline(renderer, program);
                    p.setPipeline(program.getPipeline());
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
        ShadingMaps pbrShading = new ShadingMaps(primitive.getMaterial());
        return new GLTFShaderProgram(pbrShading);
    }

    @Override
    public RenderableMesh create() throws IOException, BackendException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bounds createBounds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProgram(GraphicsShader program) {
        /**
         * This does nothing - pipelines are created in #createPrograms(GLTF)
         */
    }

    @Override
    public void setShader(Shader shader) {
        /**
         * This does nothing - pipelines are created in #createPrograms(GLTF)
         */
    }

    @Override
    public GraphicsShader createProgram() {
        /**
         * This does nothing - pipelines are created in #createPrograms(GLTF)
         */
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

    @Override
    public MeshBuilder<RenderableMesh> setShapeBuilder(ShapeBuilder shapeBuilder) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MeshBuilder<RenderableMesh> setArrayMode(DrawMode mode, int vertexCount, int vertexStride) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MeshBuilder<RenderableMesh> setTexture(ExternalReference textureRef) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MeshBuilder<RenderableMesh> setElementMode(DrawMode mode, int vertexCount, int vertexStride,
            int indiceCount) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MeshBuilder<RenderableMesh> setMaterial(Material material) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MeshBuilder<RenderableMesh> setObjectCount(int objectCount) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MeshBuilder<RenderableMesh> setMode(DrawMode mode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MeshBuilder<RenderableMesh> setAttributesPerVertex(int[] sizePerVertex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MeshBuilder<RenderableMesh> setTexture(Texture2D texture) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ShapeBuilder getShapeBuilder() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Texture2D getTexture() {
        // TODO Auto-generated method stub
        return null;
    }

}
