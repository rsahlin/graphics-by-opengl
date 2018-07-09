package com.nucleus.scene;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.common.Type;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.NodeRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.GLTF.GLTFException;

/**
 * Node containing a glTF model
 *
 */
public class GLTFNode extends Node {

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
    public NodeRenderer<GLTFNode> getNodeRenderer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean renderNode(NucleusRenderer renderer, Pass currentPass, float[][] matrices) throws GLException {
        NodeRenderer<GLTFNode> nodeRenderer = getNodeRenderer();
        if (nodeRenderer != null) {
            nodeRenderer.renderNode(renderer, this, currentPass, matrices);
        }
        return false;
    }

}
