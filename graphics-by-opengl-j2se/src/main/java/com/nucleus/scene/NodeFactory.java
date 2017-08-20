package com.nucleus.scene;

import com.nucleus.common.Type;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.MeshFactory;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.shader.ShaderProgram;

/**
 * Factory methods for node creation, implements support for new nodes in sublcasses.
 * This is used by the scene factory implementations to create nodes.
 * If the node uses Mesh(es) these must be created, allocating buffers, programs and fetching textures as needed.
 * It shall be possible to render/process the returned node.
 * 
 * @author Richard Sahlin
 *
 */
public interface NodeFactory {

    /**
     * Creates a new instance of the source node, allocating buffers, programs and fetching textures as needed.
     * If implementations do not recognize the node to be created they must call super
     * {@link #create(NucleusRenderer, Node, RootNode)}
     * Use this method when nodes are loaded from scene
     * 
     * @param renderer
     * @param meshFactory The mesh factory to use when creating mesh
     * @param source The loaded source node, the returned node shall be the same type
     * @param root The rootnode of the created node
     * @return A new instance of the source node, ready to be rendered/processed
     * @throws NodeException If there is an error creating the Node, could be due to problem reading resource or
     * compiling shaders. Will be thrown if the source node was not loaded (type field is not set)
     */
    public Node create(NucleusRenderer renderer, MeshFactory meshFactory, Node source,
            RootNode root) throws NodeException;

    /**
     * Creates a new instance of the node as specified by the classResolver, allocating buffers, programs and fetching
     * textures as needed.
     * Use this when nodes are created programmatically.
     * 
     * @param renderer
     * @param program
     * @param builder
     * @param nodeType Type and class of node to create
     * @param root
     * @return
     * @throws NodeException
     */
    public Node create(NucleusRenderer renderer, ShaderProgram program, Mesh.Builder<?> builder, Type<Node> nodeType,
            RootNode root) throws NodeException;

    /**
     * Creates the child nodes from the source node, adding the created children to the parent node.
     * The rootnode of the parent must be copied.
     * Use this method when nodes are loaded
     * 
     * @param source The source node containing the children to create
     * @param parent The destination where the created child nodes will be added.
     * @param root The rootnode of the created node
     * @throws NodeException If there is an error creating the Node, could be due to problem reading resource or
     * compiling
     * shaders
     */
    public void createChildNodes(NucleusRenderer renderer, MeshFactory meshFactory, Node source, Node parent)
            throws NodeException;


}
