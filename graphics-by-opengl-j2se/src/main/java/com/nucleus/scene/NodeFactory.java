package com.nucleus.scene;

import java.io.IOException;

import com.nucleus.renderer.NucleusRenderer;

/**
 * Factory methods for node creation, implements support for new nodes in sublcasses.
 * 
 * @author Richard Sahlin
 *
 */
public interface NodeFactory {

    public Node create(NucleusRenderer renderer, Node source, String reference, RootNode scene)
            throws IOException;

}
