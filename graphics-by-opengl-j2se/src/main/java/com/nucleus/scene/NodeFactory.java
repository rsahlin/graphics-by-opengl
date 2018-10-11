package com.nucleus.scene;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.NucleusRenderer;

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
     * {@link #create(NucleusRenderer, Node, RootNodeImpl)}
     * Use this method when nodes are loaded from scene
     * 
     * @param gles
     * @param source The loaded source node, the returned node shall be the same type
     * @param root The rootnode of the created node
     * @return A new instance of the source node, ready to be rendered/processed
     * @throws NodeException If there is an error creating the Node, could be due to problem reading resource or
     * compiling shaders. Will be thrown if the source node was not loaded (type field is not set)
     */
    public Node create(GLES20Wrapper gles, Node source, RootNodeImpl root) throws NodeException;

    /**
     * Creates the child nodes from the source node, adding the created children to the parent node.
     * The rootnode of the parent must be copied.
     * Use this method when nodes are loaded
     * 
     * @param gles
     * @param source The source node containing the children to create
     * @param parent The destination where the created child nodes will be added.
     * @throws NodeException If there is an error creating the Node, could be due to problem reading resource or
     * compiling
     * shaders
     */
    public void createChildNodes(GLES20Wrapper gles, Node source, Node parent)
            throws NodeException;

}
