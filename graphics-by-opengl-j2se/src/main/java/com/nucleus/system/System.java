package com.nucleus.system;

import com.nucleus.common.TypeResolver;
import com.nucleus.component.Component;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.RootNode;

/**
 * The system handling one or more components, one system shall handle all controllers of the same kind.
 * There shall only be one system of each kind, which can handle multiple components
 */
public abstract class System {

    private String type;

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
     * @param renderer
     * @param root
     * @param component The component to be used in the system
     */
    public abstract void initSystem(NucleusRenderer renderer, RootNode root, Component component);

    /**
     * Returns the size of data for each entity needed by the system to do processing.
     * This is called from the
     * {@link Component#create(com.nucleus.renderer.NucleusRenderer, com.nucleus.component.ComponentNode, System)}
     * method.
     * 
     * @return Size of data for each entity.
     */
    public abstract int getEntityDataSize();

    /**
     * Returns the type of component, this is tied to the implementing class by {@link TypeResolver}
     * 
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the system, this is normally only done when creating the system.
     * 
     * @param type
     * @return
     */
    protected void setType(String type) {
        this.type = type;
    }

}
