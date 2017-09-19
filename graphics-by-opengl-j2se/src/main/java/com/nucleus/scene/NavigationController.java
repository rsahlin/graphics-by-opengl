package com.nucleus.scene;

/**
 * Singleton implementation of navigation controller, handles navigation within scene
 */
public class NavigationController {

	private static NavigationController controller;
	
	/**
	 * Returns the singleton instance of the navigation controller
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
	 * @param root
	 * @param nodeId
	 * @param active
	 */
	public void setActiveSwitch(RootNode root, String nodeId, String active) {
        SwitchNode target = (SwitchNode) root.getNodeById(nodeId);
        if (target != null) {
            target.setActive(active);
        }
		
	}
	
}
