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

    private Map<String, System> componentSystem = new HashMap<>();

    private static ComponentHandler handler;

    public static ComponentHandler getInstance() {
        if (handler == null) {
            handler = new ComponentHandler();
        }
        return handler;
    }

    /**
     * Registers a component, by type, to be handled by a system.
     * When {@link #processComponent(Component, float)} is called the system specified here will be used
     * 
     * @param component The type of this component will be used to register the system.
     * @param system The system that will be registered with the components type
     */
    public void registerComponent(Component component, System system) {
        componentSystem.put(component.getType(), system);
    }

    /**
     * Processes the component with the system that has been registered to handle the component
     * A system must be registered with component by calling {@link #registerComponent(Component, System)}
     * 
     * @param component
     * @param deltaTime
     * @throws IllegalArgumentException If no system is registered for the component (type)
     */
    public void processComponent(Component component, float deltaTime) {
        System system = componentSystem.get(component.getType());
        if (system == null) {
            throw new IllegalArgumentException("No system registered for type: " + component.getType());
        }
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
