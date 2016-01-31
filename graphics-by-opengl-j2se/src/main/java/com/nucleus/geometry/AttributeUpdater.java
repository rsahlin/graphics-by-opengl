package com.nucleus.geometry;

/**
 * For usecases where the attribute data needs to be updated (in the mesh)
 * Implement this in classes that use/extend Mesh and needs to update the attribute data.
 * This interface has 2 parts - one for consumers of attribute data (normally Mesh)
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
     * This is for objects that need (consumes) attribute data, ie Meshes
     * 
     * @author Richard Sahlin
     *
     */
    public interface Consumer {
        /**
         * Copy updated generic attributes into the VertexBuffers (in a Mesh) as needed, ie the last step needed
         * before the Mesh can be rendered.
         * What data and what to copy is implementation specific and depends on the shader program used
         * to render the mesh.
         */
        public void setAttributeData();
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
     * Returns the generic attribute data as an array reference
     * 
     * @return The array containing the attribute data, any changes done here shall be reflected when
     * setAttributeData() is called.
     */
    public float[] getAttributeData();

    /**
     * Release all resources allocated by the implementing class, call this when this object shall not be used anymore.
     */
    public void destroy();
}
