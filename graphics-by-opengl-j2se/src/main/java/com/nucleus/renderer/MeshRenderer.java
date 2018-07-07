package com.nucleus.renderer;

import com.nucleus.opengl.GLException;
import com.nucleus.shader.ShaderProgram;

public interface MeshRenderer<T> {

    /**
     * Renders one mesh, material is used to fetch program and set attributes/uniforms.
     * If the attributeupdater is set in the mesh it is called to update buffers.
     * If texture exists in mesh it is made active and used.
     * If mesh contains an index buffer it is used and glDrawElements is called, otherwise
     * drawArrays is called.
     * 
     * @param renderer
     * @param program The active program
     * @param mesh The mesh to be rendered.
     * @param matrices accumulated modelview matrix for this mesh, this will be sent to uniform.
     * projectionMatrix The projection matrix, depending on shader this is either concatenated
     * with modelview set to unifom.
     * renderPassMatrix Optional matrix for renderpass
     * @throws GLException If there is an error in GL while drawing this mesh.
     */
    public abstract void renderMesh(NucleusRenderer renderer, ShaderProgram program, T mesh, float[][] matrices)
            throws GLException;

}
