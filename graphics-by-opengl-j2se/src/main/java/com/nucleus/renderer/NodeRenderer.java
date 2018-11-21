package com.nucleus.renderer;

import com.nucleus.opengl.GLException;

public interface NodeRenderer<T> {
    
    /**
     * Renders the node and children.
     * 
     * @param renderer
     * @param node The node to render, including childnodes
     * @param currentPass
     * @param matrices
     * @return True if the node was rendered - false if node does not contain any mesh or the state was such that
     * nothing was rendered.
     * @throws GLException
     */
    public boolean renderNode(NucleusRenderer renderer, T node, Pass currentPass, float[][] matrices)
            throws GLException;
    

}
