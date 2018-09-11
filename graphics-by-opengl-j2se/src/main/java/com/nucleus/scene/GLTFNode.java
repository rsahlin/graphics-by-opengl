package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.common.Type;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.shader.ShaderProgram;

/**
 * Node containing a glTF model
 *
 */
public class GLTFNode extends AbstractNode implements RenderableNode<Mesh> {

    private static final String GLTF_NAME = "glTFName";

    @SerializedName(GLTF_NAME)
    private String glTFName;

    transient private GLTF glTF;

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
            } catch (IOException | GLTFException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public ArrayList<Mesh> getMeshes(ArrayList<Mesh> list) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MeshBuilder<Mesh> createMeshBuilder(NucleusRenderer renderer, ShapeBuilder shapeBuilder)
            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void create() {
        // TODO Auto-generated method stub
    }

    @Override
    public void addMesh(Mesh mesh) {
        // TODO Auto-generated method stub
    }

    @Override
    public ShaderProgram getProgram() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProgram(ShaderProgram program) {
        // TODO Auto-generated method stub
    }

    @Override
    public ExternalReference getTextureRef() {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public void renderMesh(NucleusRenderer renderer, ShaderProgram program, Mesh mesh, float[][] matrices)
			throws GLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean renderNode(NucleusRenderer renderer, Pass currentPass, float[][] matrices) throws GLException {
		// TODO Auto-generated method stub
		return false;
	}

}
