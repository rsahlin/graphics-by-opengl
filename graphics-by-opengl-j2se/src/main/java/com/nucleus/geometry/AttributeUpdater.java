package com.nucleus.geometry;

import com.nucleus.shader.ShaderProgram;

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
        /**
         * UV information
         */
        UV(),
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
     * Holds the property indexes as they will be in a shader program.
     * 
     * @author Richard Sahlin
     *
     */
    public class PropertyMapper {
        public final int TRANSLATE_INDEX;
        public final int UV_INDEX;
        public final int ROTATE_INDEX;
        public final int SCALE_INDEX;
        public final int FRAME_INDEX;
        public final int COLOR_INDEX;
        public final int COLOR_SPECULAR_INDEX;
        public final int SPECULAR_POWER_INDEX;
        public final int ATTRIBUTES_PER_VERTEX;

        /**
         * Creates the attributer index mapping for the properties with a specific shader program.
         * 
         * @param program
         */
        public PropertyMapper(ShaderProgram program) {
            TRANSLATE_INDEX = program.getPropertyOffset(Property.TRANSLATE);
            UV_INDEX = program.getPropertyOffset(Property.UV);
            ROTATE_INDEX = program.getPropertyOffset(Property.ROTATE);
            SCALE_INDEX = program.getPropertyOffset(Property.SCALE);
            FRAME_INDEX = program.getPropertyOffset(Property.FRAME);
            COLOR_INDEX = program.getPropertyOffset(Property.COLOR);
            COLOR_SPECULAR_INDEX = program.getPropertyOffset(Property.COLOR);
            SPECULAR_POWER_INDEX = program.getPropertyOffset(Property.SPECULAR_POWER);
            ATTRIBUTES_PER_VERTEX = program.getAttributesPerVertex();
        }

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

    /**
     * Returns the propertymapper to be used to find positions of property attributes.
     * 
     * @return The propertymapper.
     */
    public PropertyMapper getMapper();

}
