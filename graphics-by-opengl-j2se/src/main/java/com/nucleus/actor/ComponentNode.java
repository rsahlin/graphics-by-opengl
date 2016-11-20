package com.nucleus.actor;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.CoreApp;
import com.nucleus.component.Component;
import com.nucleus.component.ComponentException;
import com.nucleus.io.ResourcesData;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.system.ComponentHandler;

/**
 * For nodes that contain component objects
 * Holds one or more components
 * To automatically process components set the {@link LogicProcessor} in
 * {@link CoreApp#setLogicProcessor(LogicProcessor)}
 * 
 * @author Richard Sahlin
 *
 */
public class ComponentNode extends Node implements ComponentController {

    transient public State controllerState = State.CREATED;

    @SerializedName("components")
    private ArrayList<Component> components = new ArrayList<>();
    
    /**
     * Default constructor
     */
    public ComponentNode() {
        super();
    }

    @Override
    public ComponentNode createInstance() {
        ComponentNode node = new ComponentNode();
        return node;
    }

    @Override
    public Node copy() {
        ComponentNode copy = createInstance();
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
     * Create the components
     * 
     * @param renderer
     * @param resources
     * @throws ComponentException If one or more of the components could not be created
     */
    public void createComponents(NucleusRenderer renderer, ResourcesData resources) throws ComponentException {
        for (Component c : components) {
            c.create(renderer, resources, this);
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

    public ActorContainer[] getActorContainer() {
        return null;
    }

    /**
     * Perform processing on the component, this shall take the state of the component into consideration.
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
    public State getControllerState() {
        return controllerState;
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
        controllerState = State.INITIALIZED;
    }

}
