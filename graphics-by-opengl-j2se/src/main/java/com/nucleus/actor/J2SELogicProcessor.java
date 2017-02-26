package com.nucleus.actor;

import com.nucleus.actor.ComponentController.ComponentState;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.State;

/**
 * Logic processor implementation
 * 
 * @author Richard Sahlin
 *
 */
public class J2SELogicProcessor implements LogicProcessor {

    @Override
    public void processNode(Node node, float deltaTime) {
         if (node == null) {
            return;
        }
        if (node instanceof ComponentNode) {
            ComponentNode actorNode = (ComponentNode) node;
            if (actorNode.getControllerState() == ComponentState.CREATED) {
                actorNode.init();
            }
            ActorContainer[] lcArray = actorNode.getActorContainer();
            if (lcArray != null) {
                for (ActorContainer lc : lcArray) {
                    if (lc != null) {
                        lc.process(deltaTime);
                    }
                }
            }
            actorNode.processComponents(deltaTime);
        }
        // Process children
        for (Node child : node.getChildren()) {
            if (child.getState() == null || child.getState() == State.ACTOR || child.getState() == State.ON) {
                processNode(child, deltaTime);
            }
        }
    }

}
