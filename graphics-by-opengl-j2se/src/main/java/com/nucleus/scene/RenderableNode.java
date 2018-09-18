package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.geometry.Material;
import com.nucleus.geometry.MeshBuilder.MeshBuilderFactory;
import com.nucleus.renderer.MeshRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderProgram;

/**
 * Node that can be rendered - used by nodes that shall display content on screen.
 * Note that a generic is used as Mesh type, this is to provide flexibility in how a mesh is defined and how it is rendered.
 *
 * @param <T> The mesh type
 */
public interface RenderableNode<T> extends MeshBuilderFactory<T>, Node {

    /**
     * If a custom node renderer shall be used to render the node it is returned
     * @return Custom node renderer to use or null to use default.
     */
    public com.nucleus.renderer.NodeRenderer<?> getNodeRenderer();
    
    /**
     * Retuns the meshes for this node, current meshes are copied into the list
     * 
     * @return List of added meshes
     */
    public ArrayList<T> getMeshes(ArrayList<T> list);

    /**
     * Returns the renderer to use for the Mesh type supported by the RenderableNode implementation.
     * @return
     */
    public MeshRenderer<T> getMeshRenderer();
    
    /**
     * Adds a mesh to be rendered next time the {@link #renderNode(NucleusRenderer, Pass, float[][])} method is called.
     * @param mesh
     */
    public void addMesh(T mesh);

    /**
     * Returns the program to use when rendering the meshes in this node.
     * 
     * @return
     */
    public ShaderProgram getProgram();

    /**
     * Sets the program to use when rendering.
     * 
     * @param program
     * @throws IllegalArgumentException If program is null
     */
    public void setProgram(ShaderProgram program);

    /**
     * Returns the loaded material definition for the Node
     * 
     * @return Material defined for the Node or null
     */
    public Material getMaterial();

}
