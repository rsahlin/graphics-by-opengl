package com.nucleus.scene;

import java.util.ArrayDeque;
import java.util.Deque;

import com.nucleus.scene.Node.NodeTypes;

/**
 * Singleton implementation of navigation controller, handles navigation within scene
 */
public class NavigationController {

    public static final int EMPTY = -1;

    private static class BackStackEntry {
        /**
         * The node
         */
        Node node;
        /**
         * Optional value for switch node that declare the active state.
         */
        String active;

        /**
         * Creates a new backstack entry to save the state of a switchnode.
         * 
         * @param node
         */
        private BackStackEntry(SwitchNode node) {
            this.node = node;
            this.active = node.getActive();
        }

    }

    private static NavigationController controller;
    private static Deque<BackStackEntry> stack = new ArrayDeque<>();

    /**
     * Returns the singleton instance of the navigation controller
     * 
     * @return
     */
    public static NavigationController getInstance() {
        if (controller == null) {
            controller = new NavigationController();
        }
        return controller;
    }

    /**
     * Locates the node by id, if switch node then the active switch is set and current node is pushed
     * on backstack so that navigation back is possible.
     * 
     * @param root
     * @param nodeId
     * @param active
     */
    public void setActiveSwitch(RootNode root, String nodeId, String active) {
        SwitchNode target = (SwitchNode) root.getNodeById(nodeId);
        if (target != null) {
            stack.push(new BackStackEntry(target));
            target.setActive(active);
        }

    }

    /**
     * If there is an entry on the back stack, it is popped. Returns the number of entries on the stack after popping
     * the current.
     * Returns {@link #EMPTY} if stack is empty and nothing was popped.
     * 
     * @return
     */
    public int popBackStackEntry() {
        if (stack.isEmpty()) {
            return EMPTY;
        }
        BackStackEntry back = stack.pop();
        handleBack(back);
        return stack.size();
    }

    private void handleBack(BackStackEntry back) {
        if (back.active != null) {
            SwitchNode sn = (SwitchNode) back.node;
            sn.setActive(back.active);
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

}
