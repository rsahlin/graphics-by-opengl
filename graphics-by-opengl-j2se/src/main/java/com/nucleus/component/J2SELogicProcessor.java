package com.nucleus.component;

import com.nucleus.bounds.Bounds;
import com.nucleus.component.ComponentController.ComponentState;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.State;
import com.nucleus.scene.RootNode;

/**
 * Logic processor implementation
 * 
 * @author Richard Sahlin
 *
 */
public class J2SELogicProcessor implements LogicProcessor {

    Bounds bounds;
    
    @Override
    public void processRoot(RootNode root, float delta) {
        bounds = root.getBounds();
        //Todo need to update bounds to view
        Node rootNode = root.getNodeById(RootNode.ROOTNODE_ID);
        
        for (Node node : root.getChildren()) {
            processNode(node, delta);
        }
        
    }
    
    @Override
    public void processNode(Node node, float deltaTime) {
         if (node == null) {
            return;
        }
        //Setup 
        if (!cullNode(node)) {
            //TODO check node type instead
            if (node instanceof ComponentNode) {
                ComponentNode actorNode = (ComponentNode) node;
                if (actorNode.getControllerState() == ComponentState.CREATED) {
                    actorNode.init();
                }
                actorNode.processComponents(deltaTime);
            }
        }
        // Process children
        for (Node child : node.getChildren()) {
            if (child.getState() == null || child.getState() == State.ACTOR || child.getState() == State.ON) {
                processNode(child, deltaTime);
            }
        }
    }

    /**
     * Checks if this node is within bounds, if so return true, otherwise return false
     * @param node
     * @return True if this nodes bounds are outside cull area, false if inside or no bounds.
     */
    protected boolean cullNode(Node node) {
        if (bounds == null) {
            return false;
        }
        return node.cullNode(bounds, Pass.LOGIC);
    }
    
}
