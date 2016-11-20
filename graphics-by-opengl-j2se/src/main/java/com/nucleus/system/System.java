package com.nucleus.system;

import com.nucleus.component.Component;
import com.nucleus.scene.RootNode;

/**
 * The system handling one or more components, one system shall handle all controllers of the same kind.
 */
public abstract class System {

    /**
     * Updates the component using this system.
     * 
     * @param component The component to update
     * @param deltaTime The time lapsed since last call to process
     * @throws IllegalStateException If {@link #initSystem(RootNode)} has not been called.
     */
    public abstract void process(Component component, float deltaTime);

    /**
     * Inits the system, this must be called before {@link #process(Component, float)} is called.
     * 
     * @param root
     */
    public abstract void initSystem(RootNode root);
}
