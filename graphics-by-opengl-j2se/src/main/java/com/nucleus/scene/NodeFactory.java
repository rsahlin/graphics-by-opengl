package com.nucleus.scene;

import java.io.IOException;

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
     * {@link #create(NucleusRenderer, Node, RootNode)}
     * 
     * @param renderer
     * @param source The source node, the returned node shall be same type.
     * @param scene Scene data.
     * @return A new instance of the source node, ready to be rendered/processed
     * @throws IOException If there is an error fetching shader programs or texture.
     */
    public Node create(NucleusRenderer renderer, Node source, RootNode scene)
            throws IOException;

}
