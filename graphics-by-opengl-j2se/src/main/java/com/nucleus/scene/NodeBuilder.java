package com.nucleus.scene;

import java.io.IOException;

import com.nucleus.SimpleLogger;
import com.nucleus.common.Type;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.opengl.GLException;
import com.nucleus.shader.ShaderProgram;

/**
 * Builder for Nodes, use this when nodes are created programmatically
 *
 * @param <T>
 * @param <S>
 */
public class NodeBuilder<T extends Node> {

    protected Type<Node> type;
    protected RootNode root;
    protected int meshCount = 0;
    protected MeshBuilder<Mesh> meshBuilder;
    protected ShaderProgram program;

    public NodeBuilder<?> setType(Type<Node> type) {
        this.type = type;
        return this;
    }

    public NodeBuilder<?> setRoot(RootNode root) {
        this.root = root;
        return this;
    }

    /**
     * Sets the program to use for this node.
     * 
     * @param program
     * @return
     */
    public NodeBuilder<?> setProgram(ShaderProgram program) {
        this.program = program;
        return this;
    }

    /**
     * Sets the Mesh builder to be used to create meshes, set number of meshes to build by calling
     * {@link #setMeshCount(int)}
     * 
     * @param meshBuilder
     * @return
     */
    public NodeBuilder<T> setMeshBuilder(MeshBuilder meshBuilder) {
        this.meshBuilder = meshBuilder;
        return this;
    }

    /**
     * Sets the number of meshes to create by calling the meshBuilder, default to 1
     * 
     * @param meshCount
     * @return
     */
    public NodeBuilder<T> setMeshCount(int meshCount) {
        this.meshCount = meshCount;
        return this;
    }

    /**
     * Creates an instance of Node using the specified builder parameters, first checking that the minimal
     * configuration is set.
     * 
     * @param id
     * @return
     * @throws NodeException
     * @throws {@link IllegalArgumentException} If not all needed parameters are set
     */
    public T create(String id) throws NodeException {
        try {
            if (type == null || root == null || program == null) {
                throw new IllegalArgumentException("Must set type, root and program before calling #create()");
            }
            if (meshCount > 0 && meshBuilder == null) {
                throw new IllegalArgumentException("meshCount = " + meshCount
                        + " but mesh builder is not set. either call #setMeshBuilder() or #setMeshCount(0)");
            }
            if (meshCount == 0 && meshBuilder != null) {
                // Treat this as a warning - it may be wanted behavior.
                SimpleLogger.d(getClass(), "MeshBuilder is set but meshcount is 0 - no mesh will be created");
            }
            Node node = AbstractNode.createInstance(type, root);
            if (node instanceof RenderableNode<?>) {
                ((RenderableNode<?>) node).setProgram(program);
                for (int i = 0; i < meshCount; i++) {
                    Mesh mesh = meshBuilder.create();
                    ((RenderableNode<Mesh>) node).addMesh(mesh);
                }
            }
            node.setId(id);
            node.create();
            // node.getProgram().initBuffers(mesh);
            return (T) node;
        } catch (InstantiationException | IllegalAccessException | GLException | IOException e) {
            throw new NodeException("Could not create node: " + e.getMessage());
        }
    }

}
