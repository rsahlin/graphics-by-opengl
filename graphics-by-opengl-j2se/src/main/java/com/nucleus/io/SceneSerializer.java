package com.nucleus.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.nucleus.common.Type;
import com.nucleus.geometry.MeshFactory;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.NodeFactory;
import com.nucleus.scene.RootNode;

/**
 * Create a scene node without a direct connection to the underlying implementation of how to load and parse
 * scene data.
 * 
 * @author Richard Sahlin
 *
 */
public interface SceneSerializer {

    public final static String NULL_RENDERER_ERROR = "Renderer is null.";
    public final static String NULL_NODEFACTORY_ERROR = "Node factory is null.";
    public final static String NULL_MESHFACTORY_ERROR = "Mesh factory is null.";

    public final static String INIT_NOT_CALLED_ERROR = "Init not called before import, must call #init(NucleusRenderer, NodeFactory, MeshFactory)";

    /**
     * Sets the renderer and node factory needed when scenes are imported.
     * This method must be called before importScene is called.
     * 
     * @param renderer
     * @param nodeFactory
     * @param meshFactory
     * @param types List of key/value classnames and types that can be serialized, or null
     * @throws IllegalArgumentException If renderer is null
     */
    public void init(NucleusRenderer renderer, NodeFactory nodeFactory, MeshFactory meshFactory, Type<?>[] types);

    /**
     * Registers a list of types that can be resolved to classes, these are the user defined classes serialized by
     * projects.
     * 
     * @param types List of key/value classes that shall be serialized
     * @throws IllegalArgumentException If a type already has been registered with the same name as is in the list
     */
    public void registerTypes(Type<?>[] types);

    /**
     * Returns true if the serializer is initialized by calling {@link #init(NucleusRenderer, NodeFactory, MeshFactory)}
     * 
     * @return
     */
    public boolean isInitialized();

    /**
     * Adds a node type to list of known node name/classes. Use this to add support for custom node when
     * importing/exporting.
     * 
     * @param type
     * @throws IllegalArgumentException If type has already been registered
     */
    public void addNodeType(Type<Node> type);

    /**
     * Adds a list of node types to list of known node name/classes. Use this to add support for custom node when
     * importing/exporting.
     * 
     * @param type
     * @throws IllegalArgumentException If type has already been registered
     */
    public void addNodeTypes(Type<Node>[] types);

    /**
     * Creates nodetree from a scene, the scene will be loaded using filename and the node returned shall be the root
     * node.
     * Before calling this method the renderer must be set, otherwise loading of texture and materials cannot be
     * created.
     * 
     * @param filename Name of file containing scene data.
     * @return The scene, including all defined children.
     * @throws NodeException If there is an exception loading the data.
     * @throws IllegalStateException If the renderer or nodefactory has not been set before calling this method.
     */
    public RootNode importScene(String filename) throws NodeException;

    /**
     * Creates a root node from a scene, the scene will be loaded from the inputstream.
     * Same as calling {@link #importScene(String)} but with stream instead of filename.
     * 
     * @param is Inputstream containing the scene data
     * @return The scene root node, including all defined children.
     * @throws NodeException If there is an exception loading the data.
     * @throws IllegalArgumentException If inputstream is null
     * @throws IllegalStateException If the renderer or nodefactory has not been set before calling this method.
     */
    public RootNode importScene(InputStream is) throws NodeException;

    /**
     * Exports a scene in the same format as this serializer can import.
     * 
     * @param out
     * @param obj
     * @throws IOException
     */
    public void exportScene(OutputStream out, Object obj) throws IOException;

}
