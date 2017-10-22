package com.nucleus.component;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.Type;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;
import com.nucleus.system.ComponentHandler;
import com.nucleus.system.System;

/**
 * For nodes that contain component objects.
 * Holds one or more components
 * 
 * @author Richard Sahlin
 *
 */
public class ComponentNode extends Node implements ComponentController {

    transient public ComponentState componentState = ComponentState.CREATED;

    @SerializedName("components")
    private ArrayList<Component> components = new ArrayList<>();

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    @Deprecated
    protected ComponentNode() {
    }
    /**
     * Default constructor
     */
    private ComponentNode(RootNode root) {
        super(root, NodeTypes.componentnode);
    }

    @Override
    public Node createInstance(RootNode root) {
        ComponentNode copy = new ComponentNode(root);
        copy.set(this);
        return copy;
    }

    @Override
    public void set(Node source) {
        set((ComponentNode) source);
    }

    private void set(ComponentNode source) {
        super.set(source);
        copyComponents(source.components);
    }

    /**
     * Create the components, the {@link System} needed by the component will be created and registered with the {@link ComponentHandler}
     * 
     * @param renderer
     * @throws ComponentException
     * If one or more of the components could not be created
     */
    public void createComponents(NucleusRenderer renderer) throws ComponentException {
        ComponentHandler handler = ComponentHandler.getInstance();
        try {
            for (Component c : components) {
                c.create(renderer, this);
                handler.createSystem(c);
                handler.registerComponent(c);
            }
        } catch (InstantiationException  | IllegalAccessException e) {
            throw new ComponentException(e);
        }
    }

    /**
     * Copy the components from the source array into this class
     * 
     * @param source
     */
    public void copyComponents(ArrayList<Component> source) {
        for (Component c : source) {
            components.add(c.copy());
        }
    }

    /**
     * Returns the component with matching id
     * 
     * @param id
     * @return The component with matching id or null if none found.
     */
    public Component getComponentById(String id) {
        for (Component c : components) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Perform processing on the component, this shall take the state of the
     * component into consideration.
     * 
     * @param deltaTime
     */
    public void processComponents(float deltaTime) {
        ComponentHandler handler = ComponentHandler.getInstance();
        for (Component c : components) {
            handler.processComponent(c, deltaTime);
        }
    }

    @Override
    public ComponentState getControllerState() {
        return componentState;
    }

    @Override
    public void play() {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init() {
        componentState = ComponentState.INITIALIZED;
    }

}
