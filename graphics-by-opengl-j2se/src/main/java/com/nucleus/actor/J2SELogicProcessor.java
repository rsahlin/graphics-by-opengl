package com.nucleus.actor;

import com.nucleus.actor.ComponentController.State;
import com.nucleus.scene.Node;

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
            if (actorNode.getControllerState() == State.CREATED) {
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
            processNode(child, deltaTime);
        }
    }

}
