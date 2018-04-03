package com.nucleus.system;

import java.util.HashMap;
import java.util.Map;

import com.nucleus.common.TypeResolver;
import com.nucleus.component.Component;
import com.nucleus.profiling.FrameSampler;
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

    /**
     * Lookup components from the system type
     */
    private Map<String, Component> systemComponent = new HashMap<>();
    /**
     * Lookup system from the component id
     */
    private Map<String, System> componentSystem = new HashMap<>();

    private static ComponentHandler handler;

    public static ComponentHandler getInstance() {
        if (handler == null) {
            handler = new ComponentHandler();
        }
        return handler;
    }

    /**
     * Registers a component to be handled by a system, the system handling the component must have been created
     * by calling {@link #createSystem(Component)}
     * When {@link #processComponent(Component, float)} is called the system specified here will be used
     * 
     * @param component The type of this component will be used to register the system.
     * @param system The system that will be registered with the components type
     * @throws IllegalArgumentException If component already registered, or if the component/system is null, or if the
     * id of the component is null
     */
    public void registerComponent(Component component) {
        if (component == null || component.getId() == null) {
            throw new IllegalArgumentException("Null parameter: " + component);
        }
        if (componentSystem.containsKey(component.getId())) {
            throw new IllegalArgumentException(
                    "Already registered " + component.getId() + ", for system " + component.getSystem());
        }
        systemComponent.put(component.getSystem(), component);
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
        System system = componentSystem.get(component.getSystem());
        if (system == null) {
            throw new IllegalArgumentException(
                    "No system registered for " + component.getSystem() + " componentId: " + component.getId());
        }
        long start = java.lang.System.currentTimeMillis();
        system.process(component, deltaTime);
        FrameSampler.getInstance().addTag(FrameSampler.Samples.PROCESSCOMPONENT.name() + component.getId(), start,
                java.lang.System.currentTimeMillis(), FrameSampler.Samples.PROCESSCOMPONENT.detail);
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
            if (s.initialized) {
                throw new IllegalArgumentException("Already initalized system " + s.getClass().getName());
            }
            s.initSystem(renderer, root, systemComponent.get(s.getType()));
            s.initialized = true;
        }
    }

    /**
     * Creates and registers the system for the component, if the system has already been registered for the component
     * type then the registered system is returned.
     * 
     * @param component The component to create the system for
     * @return If the system has not already been created then it is created, otherwise the registered system is
     * returned.
     * @throws IllegalAccessException | InstantiationException If the system could not be created
     */
    public System createSystem(Component component) throws IllegalAccessException, InstantiationException {
        if (componentSystem.containsKey(component.getSystem())) {
            return componentSystem.get(component.getSystem());
        }
        System system = (System) TypeResolver.getInstance().create(component.getSystem());
        system.setType(component.getSystem());
        componentSystem.put(component.getSystem(), system);
        return system;
    }

    /**
     * Returns the System for the component, if one is registered
     * 
     * @param component
     * @return The System for the component, or null if not registered.
     */
    public System getSystem(Component component) {
        return componentSystem.get(component.getSystem());
    }

}
