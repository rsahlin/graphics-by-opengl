package com.nucleus.renderer;

import com.nucleus.opengl.GLException;
import com.nucleus.scene.RenderableNode;
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
    public void renderMesh(NucleusRenderer renderer, ShaderProgram program, T mesh, float[][] matrices)
            throws GLException;

    /**
     * Renders the meshes in the node, this will go through the meshes in the node and call {@link #renderMesh(NucleusRenderer, ShaderProgram, Object, float[][])} on each.
     * The shader program to use when rendering this mesh must be set before calling this method.
     * 
     * 
     * @param renderer
     * @param program Shader program used to update attributes and uniforms
     * @param node
     * @param matrices
     * @return true If one or more meshes was rendered
     * @throws GLException
     */
    public boolean renderMeshes(NucleusRenderer renderer, ShaderProgram program, RenderableNode<T> node, float[][] matrices) throws GLException;
    
}
