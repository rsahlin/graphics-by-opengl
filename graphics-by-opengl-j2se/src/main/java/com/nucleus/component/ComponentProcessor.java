package com.nucleus.component;

import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;

/**
 * Processes the component logic contained in the scenegraph
 * 
 * @author Richard Sahlin
 *
 */
public interface ComponentProcessor {

    /**
     * Starts processing components for the rootnode, this shall call {@link #processNode(Node, float)} on all of the nodes
     * that shall be processed.
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
