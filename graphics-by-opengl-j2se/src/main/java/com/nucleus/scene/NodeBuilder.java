
package com.nucleus.scene;

import java.io.IOException;

import com.nucleus.SimpleLogger;
import com.nucleus.common.Type;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.opengl.GLException;
import com.nucleus.shader.ShaderProgram;

/**
 * Builder for Nodes, use this when nodes are created programmatically.
 * The name used will be the short class name to lowercase.
 *
 * @param <T>
 */
public class NodeBuilder<T extends Node> {

    public static class ClassType implements Type<Node> {

        protected Class<? extends Node> clazz;
        
        public ClassType(Class<? extends Node> clazz) {
            this.clazz = clazz;
        }
        
        @Override
        public Class<? extends Node> getTypeClass() {
            return clazz;
        }

        @Override
        public String getName() {
            return clazz.getSimpleName().toLowerCase();
        }
        
    }
    
    protected Type<Node> type;
    protected RootNode root;
    protected int meshCount = 0;
    protected MeshBuilder<?> meshBuilder;
    protected ShaderProgram program;

    /**
     * Inits this biulder to create a new instance of the specified Node
     * @param source
     * @return
     */
    public NodeBuilder<T> init(Node source) {
        return this;
    }
    
    /**
     * Sets the node class type to create.
     * @param type
     * @return
     */
    public NodeBuilder<T> setType(Type<Node> type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the root node
     * @param root
     * @return
     */
    public NodeBuilder<T> setRoot(RootNode root) {
        this.root = root;
        return this;
    }

    /**
     * Sets the program to use for this node.
     * 
     * @param program
     * @return
     */
    public NodeBuilder<T> setProgram(ShaderProgram program) {
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
    public NodeBuilder<T> setMeshBuilder(MeshBuilder<?> meshBuilder) {
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
            if (type == null || root == null) {
                throw new IllegalArgumentException("Must set type and root before calling #create()");
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
            node.setId(id);
            node.create();
            NodeBuilder.createMesh(meshBuilder, node, meshCount);
            // node.getProgram().initBuffers(mesh);
            return (T) node;
        } catch (InstantiationException | IllegalAccessException | GLException | IOException e) {
            throw new NodeException("Could not create node: " + e.getMessage());
        }
    }

    /**
     * Builder method for one or more Meshes belonging to a Node
     * 
     * @param meshBuilder
     * @param node
     * @param meshCount
     * @throws IOException
     * @throws GLException
     */
    public static void createMesh(MeshBuilder meshBuilder,Node node, int meshCount) throws IOException, GLException {
        if (node instanceof RenderableNode<?>) {
            RenderableNode<?> rNode = (RenderableNode<?>) node;
            for (int i = 0; i < meshCount; i++) {
                if (meshBuilder != null) {
                    meshBuilder.create(rNode);
                    if (rNode.getBounds() == null) {
                        rNode.setBounds(meshBuilder.createBounds());
                    }
                }
            }
        }
    }
    
}
