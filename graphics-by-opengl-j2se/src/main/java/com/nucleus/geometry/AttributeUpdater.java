package com.nucleus.geometry;

/**
 * For usecases where the attribute data needs to be updated (in the mesh), ie it is not sufficient to set static
 * attribute data.
 * Implement this in classes that use/extend Mesh and needs to update the attribute data.
 * This interface has 2 parts - one for consumers of attribute data, ie the shader program and Mesh,
 * and one for producers of attribute data, for instance an Actor that uses a sprite that will affect the attribute
 * data being used by the Mesh.
 * This can for instance be that data from an actor changes the position/rotation/scale of a sprite, this must
 * be updated in the attribute data.
 * 
 * @author Richard Sahlin
 *
 */
public interface AttributeUpdater {

    /**
     * Enum of properties that producer / consumer can use to find offset into attribute data
     *
     */
    public enum Property {
        TRANSLATE(),
        ROTATE(),
        SCALE(),
        /**
         * Frame information, number, coordinates etc
         */
        FRAME(),
        /**
         * (Diffuse) Color
         */
        COLOR(),
        /**
         * Specular reflection property
         */
        COLOR_SPECULAR(),
        /**
         * Ambient color
         */
        COLOR_AMBIENT(),
        SPECULAR_POWER();

    }

    /**
     * This is for objects that need (consumes) attribute data, ie Meshes
     * 
     * @author Richard Sahlin
     *
     */
    public interface Consumer {
        public final static String BUFFER_NOT_BOUND = "Buffer not bound";

        /**
         * Copy updated generic attributes into the VertexBuffers (in a Mesh) as needed, ie the last step needed
         * before the Mesh can be rendered.
         * What data and what to copy is implementation specific and depends on the shader program used
         * to render the mesh.
         * 
         * @throws IllegalArgumentException If {@link #bindAttributeBuffer(VertexBuffer)} has not been called before
         * calling this method.
         */
        public void setAttributeData();

        /**
         * Returns the generic attribute data as an array reference
         * TODO Maybe it should not be visible to implementing classes that this buffer exist? It should be enough with
         * {@link #bindAttributeBuffer(VertexBuffer)} and {@link #setAttributeData()}
         * 
         * @return The array containing the attribute data, any changes done here shall be reflected when
         * setAttributeData() is called.
         * @throws IllegalArgumentException If {@link #bindAttributeBuffer(VertexBuffer)} has not been called before
         * calling this method.
         */
        public float[] getAttributeData();

        /**
         * Binds the attribute buffer to be used as a destination when set attribute data is called.
         * This method must be called before calling {@link #setAttributeData()}.
         * Implementations may need to allocate buffers.
         * 
         * @param buffer
         */
        public void bindAttributeBuffer(VertexBuffer buffer);

    }

    /**
     * For objects producing attribute data, normally Actor/Sprite
     * 
     * @author Richard Sahlin
     *
     */
    public interface Producer {
        /**
         * Update the attribute buffers according to the object producing the attribute data, this is normally
         * implemented by Nodes that hold the actors (that are using the Mesh(es) that needs the attributes updated.
         */
        public void updateAttributeData();
    }

    /**
     * Release all resources allocated by the implementing class, call this when this object shall not be used anymore.
     */
    public void destroy();
}
