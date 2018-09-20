package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.bounds.Bounds;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.exporter.Reference;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderPass;
import com.nucleus.vecmath.Transform;

public interface Node extends Reference {

    /**
     * The states a node can be in, this controls if node is rendered etc.
     * This can be used to skip nodes from being rendered or processed.
     * Enum values are bitwise
     * 
     * @author Richard Sahlin
     *
     */
    public enum State {

        /**
         * Node is on, rendered and actors processed
         */
        ON(1),
        /**
         * Node is off, not rendered and no actors processed
         */
        OFF(2),
        /**
         * Node is rendered, but no actors processed
         */
        RENDER(4),
        /**
         * Node is not rendered, but actors processed
         */
        ACTOR(8);

        public final int value;

        private State(int value) {
            this.value = value;
        }

    }

    /**
     * Creates a new instance of this node, then copies the data from this node into the copy.
     * This is a shallow copy, it does not copy children.
     * Use this for instance when nodes are loaded
     * 
     * @param root Root of the created node
     * @return New copy of this node, transient values and children will not be copied.
     * @throws IllegalArgumentException If root is null
     */
    public abstract Node createInstance(RootNode root);

    /**
     * This shall be set if node is created using {@link #createInstance(RootNode)}
     * 
     * @param type
     */
    public void setType(Type<Node> type);

    /**
     * Returns the type of node, this is a String representation that must be understood by the implementation
     * 
     * @return
     */
    public String getType();

    /**
     * Returns the root node for this node, this is the document root.
     * 
     * @return The document root.
     */
    public RootNode getRootNode();

    /**
     * Sets the rootnode for this node, this shall normally not be changed after it has been set.
     * This method shall not be called, it is used when a new instance is created using
     * {@link #createInstance(RootNode)}
     * 
     * @param root
     * @throws IllegalArgumentException If root is null
     */
    public void setRootNode(RootNode root);

    /**
     * Returns the transform for this node.
     * 
     * @return
     */
    public Transform getTransform();

    /**
     * Returns the first (closest) parent node that has defined ViewFrustum
     * 
     * @return Closest parent node that has defined ViewFrustum, or null if not found
     */
    public Node getParentView();

    /**
     * Returns the first (closest from this node) {@linkplain LayerNode} parent.
     * The search starts with the parent node of this node, if that is not a {@linkplain LayerNode} that nodes parent
     * is checked, continuing until root node.
     * 
     * @return The parent layer of this node, or null if none could be found
     */
    public LayerNode getParentLayer();

    /**
     * Adds a child at the end of the list of children.
     * The child node's parent will be set to this node.
     * 
     * @param child The child to add to this node.
     * @throws IllegalArgumentException If child does not have the root node, or id set, or if a child already has been
     * added
     * with the same id
     */
    public void addChild(Node child);

    /**
     * Returns the first node with matching type, or null if none found.
     * This method will search through the active children.
     * 
     * @param type
     * @return
     */
    public Node getNodeByType(String type);

    /**
     * Searches through the scene children and looks for the first node with matching type.
     * 
     * @param type
     * @return
     */
    public Node getNodeByType(Type<Node> type);

    /**
     * Returns the child node with matching id from this node, children are not searched recursively.
     * TODO Shall this method call getChildren() which will return only on-switched nodes?
     * 
     * @param id
     * @return The child from this node with matching id, or null if not found.
     */
    public Node getChildById(String id);

    /**
     * Returns node with matching id, searching through this node and recursively searching through children.
     * Children will be searched by calling {@link #getChildren()} excluding nodes that are switched off.
     * 
     * @param id Id of node to return
     * @return First instance of node with matching id, or null if none found
     */
    public Node getNodeById(String id);

    /**
     * Sets the state of this node, and the state of childnodes.
     * 
     * @param state
     */
    public void setState(State state);

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
     * Returns the bounds for this node if set, otherwise null
     * 
     * @return
     */
    public Bounds getBounds();

    /**
     * Sets the bounds reference
     * 
     * @param bounds Reference to bounds, values are not copied.
     */
    public void setBounds(Bounds bounds);

    /**
     * Sets the parent of this node
     * 
     * @param parent
     */
    public void setParent(Node parent);

    /**
     * Returns the children of this node
     * 
     * @return
     */
    public ArrayList<Node> getChildren();

    /**
     * Returns the property for the key, if the key has no value then defaultValue is returned.
     * 
     * @param key
     * @param defaultValue The value to return if key is not set, may be null.
     * @return The property value for key, or defaultValue if not set.
     */
    public String getProperty(String key, String defaultValue);

    /**
     * Sets the property key/value - any previous value will be overwritten.
     * 
     * @param key
     * @param value
     */
    public void setProperty(String key, String value);

    /**
     * Called when node has been created and added to parent, if Node has Mesh it has been created.
     * Do not call childrens {@link #onCreated()} recursively from this method.
     * Implement in subclasses to perform actions when the node has been created, this will be called before children of this node has been created.
     */
    public void onCreated();

    /**
     * Creates the transient values needed in runtime, this is called before any mesh is created.
     */
    public void createTransient();

    /**
     * Releases all resources held by this node
     * 
     * @param renderer
     */
    public void destroy(NucleusRenderer renderer);

    /**
     * Sets the renderpass this node is active in.
     * 
     * @param pass
     */
    public void setPass(Pass pass);

    /**
     * Returns the Pass(es) that this node should be used in
     * 
     * @return
     */
    public Pass getPass();

    /**
     * Returns the state of the node, the specifies if the node is on or off, only actor or only render
     * 
     * @return The state, or null if not set
     */
    public State getState();

    /**
     * Multiply the concatenated model matrix with this nodes transform matrix and store in this nodes model matrix
     * If this node does not have a transform an identity matrix is used.
     * 
     * @param concatModel The concatenated model matrix
     * @return The node matrix - this nodes transform * concatModel
     */
    public float[] concatModelMatrix(float[] concatModel);

    /**
     * Sets the renderpass in this node, removing any existing renderpasses.
     * Checks that the renderpasses are valid
     * TODO - look into this method to see if it is needed or if it should be moved to {@link RenderableNode}
     * 
     * @param renderPass, or null to remove renderpass
     */
    public void setRenderPass(ArrayList<RenderPass> renderPass);

    /**
     * Returns the renderpasses definition, or null if not defined.
     * TODO - look into this method to see if it is needed or if it should be moved to {@link RenderableNode}
     * 
     * @return
     */
    public ArrayList<RenderPass> getRenderPass();

    /**
     * Checks if this node is hit by the position.
     * If {@value State#ON} or {@value State#ACTOR} then the bounds are checked for intersection by the point.
     * 
     * @param position
     * @return If node is in an enabled state, has bounds and the position is inside then true is returned, otherwise
     * false
     */
    public boolean isInside(float[] position);

    /**
     * Checks if this node should be culled, returns true if this node is culled.
     * It is up to the node implementations to decide if children should be checked, default behavior is to not call
     * {@link #cullNode(Bounds, Pass)} on children, ie they should be culled separately.
     * 
     * @param cullBounds The bounds to check against
     * @param pass The renderpass to cull this node for
     * @return True if the node should be culled
     */
    public boolean cullNode(Bounds cullBounds, Pass pass);

}
