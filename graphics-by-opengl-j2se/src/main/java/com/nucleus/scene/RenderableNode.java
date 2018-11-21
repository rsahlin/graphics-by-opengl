package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.MeshBuilder.MeshBuilderFactory;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderPass;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.vecmath.Transform;

/**
 * Node that can be rendered - used by nodes that shall display content on screen.
 * Note that a generic is used as Mesh type, this is to provide flexibility in how a mesh is defined and how it is
 * rendered.
 *
 * @param <T> The mesh type
 */
public interface RenderableNode<T> extends MeshBuilderFactory<T>, Node {

    /**
     * If a custom node renderer shall be used to render the node it is returned
     * 
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
     * Adds a mesh to be rendered next time the {@link #renderNode(NucleusRenderer, Pass, float[][])} method is called.
     * 
     * @param mesh
     */
    public void addMesh(T mesh);

    /**
     * Returns the program to use when rendering the meshes in this node.
     * TODO - consider moving this into Material
     * 
     * @return
     */
    public ShaderProgram getProgram();

    /**
     * Sets the program to use when rendering.
     * TODO - consider moving this into Material
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

    /**
     * Returns a reference to the viewfrustum if defined.
     * 
     * @return View frustum or null
     */
    public ViewFrustum getViewFrustum();

    /**
     * Fetches the projection matrix for the specified pass, if set.
     * 
     * @param pass
     * @return Projection matrix for this node and childnodes, or null if not set
     */
    public float[] getProjection(Pass pass);

    /**
     * Sets the viewfrustum as a reference to the specified source
     * Note this will reference the source {@link ViewFrustum} any changes will be reflected here
     * The viewfrustum matrix will be set in the projection for this node, call {@link #getProjection()} to
     * get the matrix
     * 
     * @param source The frustum reference
     */
    public void setViewFrustum(ViewFrustum source);

    /**
     * Returns the transform for this node.
     * 
     * @return
     */
    public Transform getTransform();

    /**
     * Sets the renderpass in this node, removing any existing renderpasses.
     * Checks that the renderpasses are valid
     * 
     * @param renderPass, or null to remove renderpass
     */
    public void setRenderPass(ArrayList<RenderPass> renderPass);

    /**
     * Returns the renderpasses definition, or null if not defined.
     * 
     * @return
     */
    public ArrayList<RenderPass> getRenderPass();

    /**
     * Multiply the concatenated model matrix with this nodes transform matrix and store in this nodes model matrix
     * If this node does not have a transform an identity matrix is used.
     * 
     * @param concatModel The concatenated model matrix
     * @return The node matrix - this nodes transform * concatModel. This is a reference to the matrix in this node.
     */
    public float[] concatModelMatrix(float[] concatModel);

}
