package com.nucleus.io;

import java.io.IOException;
import java.io.OutputStream;

import com.nucleus.common.Type;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.RootNode;

/**
 * Create a scene node without a direct connection to the underlying implementation of how to load and parse
 * scene data.
 * 
 * @author Richard Sahlin
 *
 */
public interface SceneSerializer {

    public final static String NULL_GLES_ERROR = "GLES is null.";
    public final static String NULL_MESHFACTORY_ERROR = "Mesh factory is null.";

    public final static String INIT_NOT_CALLED_ERROR = "Init not called before import, must call #init()";

    /**
     * Sets the GLES wrapper needed when scenes are imported.
     * This method must be called before importScene is called.
     * 
     * @param gles
     * @param types List of key/value classnames and types that can be serialized, or null
     * @throws IllegalArgumentException If renderer is null
     */
    public void init(GLES20Wrapper gles, Type<?>[] types);

    /**
     * Registers a list of types that can be resolved to classes, these are the user defined classes serialized by
     * projects.
     * 
     * @param types List of key/value classes that shall be serialized
     * @throws IllegalArgumentException If a type already has been registered with the same name as is in the list
     */
    public void registerTypes(Type<?>[] types);

    /**
     * Returns true if the serializer has been initialized by calling {@link #init(GLES20Wrapper, Type[])}
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
     * @param path Path to asset folder, this is the root folder where assets are located
     * @param filename Name of file containing scene data.
     * @return The scene, including all defined children.
     * @throws NodeException If there is an exception loading the data.
     * @throws IllegalStateException If the renderer or nodefactory has not been set before calling this method.
     */
    public RootNode importScene(String path, String filename) throws NodeException;

    /**
     * Exports a scene in the same format as this serializer can import.
     * 
     * @param out
     * @param obj
     * @throws IOException
     */
    public void exportScene(OutputStream out, Object obj) throws IOException;

}
