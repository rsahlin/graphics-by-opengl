package com.nucleus.system;

import java.util.HashMap;
import java.util.Map;

import com.nucleus.component.Component;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.RootNode;

/**
 * Connects the components to system that is registered to handle it.
 * This class is a singleton
 * 
 * @author Richard Sahlin
 *
 */
public class ComponentHandler {

    private Map<Component, System> componentSystem = new HashMap<>();

    private static ComponentHandler handler;

    public static ComponentHandler getInstance() {
        if (handler == null) {
            handler = new ComponentHandler();
        }
        return handler;
    }

    /**
     * Registers a component to be handled by a system.
     * 
     * @param component
     * @param system
     */
    public void registerComponent(Component component, System system) {
        componentSystem.put(component, system);
    }

    /**
     * Processes the component with the system that has been registered to handle the component
     * 
     * @param component
     * @param deltaTime
     */
    public void processComponent(Component component, float deltaTime) {
        System system = componentSystem.get(component);
        system.process(component, deltaTime);
    }

    /**
     * Initializes the registered systems, this must be called before {@link #processComponent(Component, float)}
     * is called.
     * 
     * @param root
     * @param renderer
     */
    public void initSystems(RootNode root, NucleusRenderer renderer) {
        for (System s : componentSystem.values()) {
            s.initSystem(root);
        }
    }

}
