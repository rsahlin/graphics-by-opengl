package com.nucleus.component;

import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;

/**
 * Processes the logic contained in the scenegraph
 * 
 * @author Richard Sahlin
 *
 */
public interface LogicProcessor {

    /**
     * Starts processing logic for the rootnode, this shall call {@link #processNode(Node, float)} on all of the nodes
     * that shall
     * be processed.
     * Perform init if needed in this method
     * 
     * @param root
     * @param delta
     */
    public void processRoot(RootNode root, float delta);

    /**
     * Processes the non-culled logic nodes
     * 
     * @param node
     * @param delta
     */
    public void processNode(Node node, float delta);
}
