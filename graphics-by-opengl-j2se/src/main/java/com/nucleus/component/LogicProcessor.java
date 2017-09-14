package com.nucleus.component;

import com.nucleus.scene.Node;

/**
 * Processes the logic contained in the scenegraph
 * 
 * @author Richard Sahlin
 *
 */
public interface LogicProcessor {

    /**
     * Processes the non-culled logic nodes
     * 
     * @param node
     * @param delta
     */
    public void processNode(Node node, float delta);
}
