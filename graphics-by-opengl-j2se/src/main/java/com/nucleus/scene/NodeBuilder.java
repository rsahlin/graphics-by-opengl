
package com.nucleus.scene;

import java.io.IOException;

import com.nucleus.SimpleLogger;
import com.nucleus.common.Type;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.shader.ShaderProgram;

/**
 * Builder for Nodes, use this when nodes are created programmatically or instantiated from loaded resources.
 *
 * @param <T>
 */
public class NodeBuilder<T extends Node> {

    protected Type<Node> type;
    protected RootNode root;
    protected int meshCount = 0;
    protected MeshBuilder<?> meshBuilder;
    protected ShaderProgram program;

    /**
     * Inits this biulder to create a new instance of the specified Node
     * 
     * @param source
     * @return
     */
    public NodeBuilder<T> init(Node source) {
        return this;
    }

    /**
     * Sets the node class type to create.
     * 
     * @param type
     * @return
     */
    public NodeBuilder<T> setType(Type<Node> type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the root node
     * 
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
     * Creates an new instance of a Node using the specified builder parameters, first checking that the minimal
     * configuration is set.
     * Specify type class of Node by calling {@link #setType(Type)}
     * Use this method to programatically create new Nodes
     * 
     * @param id The id of the node
     * @return New instance of Node
     * @throws NodeException
     * @throws IllegalArgumentException If not all needed parameters are set
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
            Node node = createInstance(type, root);
            node.setId(id);
            node.createTransient();
            createMesh(meshBuilder, node, meshCount);
            node.onCreated();
            return (T) node;
        } catch (InstantiationException | IllegalAccessException | GLException | IOException e) {
            throw new NodeException("Could not create node: " + e.getMessage());
        }
    }

    /**
     * Creates a new, empty, instance of the specified nodeType. The type will be set.
     * Do not call this method directly, use {@link #create(String)} or {@link #create(GLES20Wrapper, Node, Node)}
     * 
     * @param nodeType
     * @paran root The root of the created instance
     * @return
     * @throws IllegalArgumentException If nodeType or root is null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    Node createInstance(Type<Node> nodeType, RootNode root) throws InstantiationException, IllegalAccessException {
        if (nodeType == null || root == null) {
            throw new IllegalArgumentException("Null parameter:" + nodeType + ", " + root);
        }
        Node node = (Node) nodeType.getTypeClass().newInstance();
        node.setType(nodeType);
        node.setRootNode(root);
        return node;
    }

    /**
     * Creates a new instance of the source Node.
     * If node is RenderableNode and the meshbuilder is set then it is used to create mesh.
     * If RenderableNode but meshbuilder not set the
     * {@link RenderableNode#createMeshBuilder(GLES20Wrapper, com.nucleus.geometry.shape.ShapeBuilder)}
     * is called to create MeshBuilder.
     * Node is added to parent.
     * 
     * @param gles
     * @param source
     * @param parent
     * @param builder
     * @return
     * @throws NodeException
     */
    public T create(GLES20Wrapper gles, T source, T parent) throws NodeException {
        T created = (T) source.createInstance(root);
        created.createTransient();
        try {
            parent.addChild(created);
            if (source instanceof RenderableNode<?>) {
                MeshBuilder<?> mBuilder = meshBuilder;
                RenderableNode rNode = (RenderableNode<?>) created;
                if (mBuilder == null) {
                    mBuilder = rNode.createMeshBuilder(gles, null);
                }
                if (mBuilder != null) {
                    createMesh(mBuilder, created, 1);
                }
            }
            created.onCreated();
            // Recursively create children if there are any
            if (source.getChildren() != null) {
                for (Node nd : source.getChildren()) {
                    create(gles, (T) nd, created);
                }
            }
            return created;
        } catch (IOException | GLException e) {
            throw new NodeException(e);
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
    protected void createMesh(MeshBuilder meshBuilder, Node node, int meshCount) throws IOException, GLException {
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
            if (rNode.getProgram() == null) {
                rNode.setProgram(meshBuilder.createProgram());
            }
        }
    }

}
