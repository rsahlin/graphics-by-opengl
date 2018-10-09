package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.TypeResolver;
import com.nucleus.component.Component;
import com.nucleus.component.ComponentController;
import com.nucleus.component.ComponentException;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.system.ComponentHandler;
import com.nucleus.system.System;

/**
 * For nodes that contain component objects.
 * Holds one or more components
 * 
 * @author Richard Sahlin
 *
 */
public class ComponentNode extends AbstractMeshNode<Mesh> implements ComponentController {

    /**
     * Builder for Nodes, use this when nodes are created programmatically
     *
     * @param <T>
     */
    public static class Builder extends NodeBuilder<ComponentNode> {

        private String component;
        private String system;

        public Builder setComponent(String component) {
            this.component = component;
            return this;
        }

        public Builder setSystem(String system) {
            this.system = system;
            return this;
        }

        @Override
        public ComponentNode create(String id) throws NodeException {
            ComponentNode node = super.create(id);
            Component component;
            try {
                component = (Component) TypeResolver.getInstance().create(this.component);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new NodeException(e);
            }
            node.addComponent(component);
            // node.createComponents(renderer);
            return node;
        }

    }

    transient public ComponentState componentState = ComponentState.CREATED;

    @SerializedName("components")
    private ArrayList<Component> components = new ArrayList<>();

    /**
     * Creates a nodebuilder that can be used to create ComponentNodes
     * 
     * @param renderer
     * @param nodeBuilder
     * @return
     */
    public NodeBuilder<ComponentNode> createBuilder(NucleusRenderer renderer, ComponentNode source) {
        Builder nodeBuilder = new Builder();
        return nodeBuilder;
    }

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

    protected void addComponent(Component component) {
        components.add(component);
    }

    @Override
    public void set(AbstractNode source) {
        set((ComponentNode) source);
    }

    private void set(ComponentNode source) {
        super.set(source);
        copyComponents(source.components);
    }

    /**
     * Create the components, the {@link System} needed by the component will be created and registered with the
     * {@link ComponentHandler}
     * 
     * @param gles
     * @throws ComponentException
     * If one or more of the components could not be created
     */
    public void createComponents(GLES20Wrapper gles) throws ComponentException {
        ComponentHandler handler = ComponentHandler.getInstance();
        try {
            for (Component c : components) {
                handler.createSystem(c);
                handler.registerComponent(c);
                c.create(gles, this);
            }
        } catch (InstantiationException | IllegalAccessException e) {
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

    @Override
    public void createTransient() {
        // TODO Auto-generated method stub

    }

    @Override
    public MeshBuilder<Mesh> createMeshBuilder(GLES20Wrapper gles, ShapeBuilder shapeBuilder)
            throws IOException {
        try {
            createComponents(gles);
        } catch (ComponentException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
