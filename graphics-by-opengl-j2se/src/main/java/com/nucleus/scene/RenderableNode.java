package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.geometry.Material;
import com.nucleus.geometry.MeshBuilder.MeshBuilderFactory;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.MeshRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.NodeRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderProgram;

/**
 * Node that can be rendered - used by nodes that shall display content on screen.
 * Note that a generic is used as Mesh type, this is to provide flexibility in how a mesh is defined and how it is rendered.
 * To be able to render a node, a matching {@link NodeRenderer} implementation must be returned by the {@link #getNodeRenderer()} method.
 *
 * @param <T> The mesh type
 */
public interface RenderableNode<T> extends MeshBuilderFactory<T>, Node {

    /**
     * Renders the mesh(es) in this node.
     * Shall not render childnodes
     * 
     * @param renderer
     * @param currentPass
     * @param matrices
     * @return True if the node was rendered - false if node does not contain any mesh or the state was such that
     * nothing was rendered.
     * @throws GLException
     */
    public abstract boolean renderNode(NucleusRenderer renderer, Pass currentPass, float[][] matrices)
            throws GLException;

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
     * Returns the external reference of the texture for this node, this is used when importing
     * 
     * @return
     */
    public ExternalReference getTextureRef();

    /**
     * Returns the loaded material definition for the Node
     * 
     * @return Material defined for the Node or null
     */
    public Material getMaterial();

}
