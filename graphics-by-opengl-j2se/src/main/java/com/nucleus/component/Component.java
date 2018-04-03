package com.nucleus.component;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.Type;
import com.nucleus.io.BaseReference;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.system.System;

/**
 * The component part of behavior, this holds the data needed to perform actions.
 * Component describes what actions are possible on an entity by defining the data.
 * This holds the data needed to process the behavior, this processing shall be done by the system handling the
 * component.
 * This is to ensure that behavior is data driven and uses composition rather than inheritance.
 * In order to be compatible with hardware acceleration such as OpenCL it is important NOT to break down into too
 * small components and to use buffers that are available on the native side (GPU)
 */
public abstract class Component extends BaseReference {

    public final static String INVALID_DATACOUNT_ERROR = "Invalid datacount";
    public final static String TYPE = "type";
    public final static String SYSTEM = "system";

    @SerializedName(TYPE)
    private String type;

    @SerializedName(SYSTEM)
    private String system;

    transient protected ArrayList<ComponentBuffer> buffers = new ArrayList<>();

    /**
     * Used to create a new instance of a component
     * 
     * @return
     */
    public abstract Component createInstance();

    /**
     * Creates the data and objects needed for the component - when this method returns the component shall be ready to
     * be used.
     * 
     * @param renderer
     * @param parent
     * @param system The system that will handle processing of the component
     * @throws ComponentException If there is an error preventing the component to be created
     */
    public abstract void create(NucleusRenderer renderer, ComponentNode parent, System system)
            throws ComponentException;

    public String getSystem() {
        return system;
    }

    public String getType() {
        return type;
    }

    public Component copy() {
        Component copy = createInstance();
        copy.set(this);
        return copy;
    }

    public void set(Component source) {
        this.type = source.type;
        this.system = source.system;
        this.setId(source.getId());
    }

    /**
     * Returns a new instance of the component of the specified type.
     * 
     * @param typeClass
     * @return A new instance of the component.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public Component create(Type<?> typeClass) throws InstantiationException, IllegalAccessException {
        return (Component) typeClass.getTypeClass().newInstance();
    }

    /**
     * Adds a buffer at the specified index, fetch by calling {@link #getBuffer(int)}
     * 
     * @param index
     * @param buffer
     */
    protected void addBuffer(int index, ComponentBuffer buffer) {
        buffers.add(index, buffer);
    }

    /**
     * Returns the buffer at index - this should normally not be used directly by clients.
     * Use
     * 
     * @param index
     * @return
     */
    protected ComponentBuffer getBuffer(int index) {
        return buffers.get(index);
    }

}
