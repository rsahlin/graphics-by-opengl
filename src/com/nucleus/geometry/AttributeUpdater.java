package com.nucleus.geometry;

/**
 * For usecases where the attribute data needs to be updated (in the mesh)
 * Implement this in classes that store a mesh and needs to update the attribute data.
 * 
 * @author Richard Sahlin
 *
 */
public interface AttributeUpdater {
    /**
     * Copy updated generic attributes into the mesh, into the VertexBuffers as needed.
     * What data and what to copy is implementation specific and depends on the shader program used
     * to render the mesh.
     */
    public Mesh setAttributes();
}
