package com.nucleus.component;

import com.nucleus.bounds.Bounds;
import com.nucleus.component.ComponentController.ComponentState;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.ComponentNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.State;
import com.nucleus.scene.RootNode;

/**
 * Component logic processor implementation
 * 
 * @author Richard Sahlin
 *
 */
public class J2SEComponentProcessor implements ComponentProcessor {

    Bounds bounds;

    @Override
    public void processRoot(RootNode root, float delta) {
        // bounds = root.getBounds();
        for (Node node : root.getChildren()) {
            processNode(node, delta);
        }

    }

    @Override
    public void processNode(Node node, float deltaTime) {
        if (node == null) {
            return;
        }
        // Setup
        if (!cullNode(node)) {
            // TODO check node type instead
            if (node instanceof ComponentNode) {
                ComponentNode componentNode = (ComponentNode) node;
                if (componentNode.getControllerState() == ComponentState.CREATED) {
                    componentNode.init();
                }
                componentNode.processComponents(deltaTime);
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
     * 
     * @param node
     * @return True if this nodes bounds are outside cull area, false if inside or no bounds.
     */
    protected boolean cullNode(Node node) {
        if (bounds == null) {
            return false;
        }
        return node.cullNode(bounds, Pass.MAIN);
    }

}
