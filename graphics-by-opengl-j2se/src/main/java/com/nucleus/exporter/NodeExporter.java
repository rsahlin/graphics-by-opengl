package com.nucleus.exporter;

import com.nucleus.common.Key;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;

/**
 * Methods for exporting nodes, this is used by scene factory implementations when exporting a scene.
 * This will not serialize the nodes but prepare and collect the necesary data so that the Node can be serialized.
 * 
 * @author Richard Sahlin
 *
 */
public interface NodeExporter {

    final static String ALREADY_REGISTERED_TYPE = "Already registered exporter for type: ";

    /**
     * Registers the node exporter for the specified node type
     * 
     * @param type The node type to register the exporter for
     * @param exporter The exporter to register, during scene export when a node with matching key is encountered this
     * exporter will be
     * called
     * @throws IllegalArgumentException If a exporter already has been registered for the type
     * @throws NullPointerException If type is null
     */
    public void registerNodeExporter(Key type, NodeExporter exporter);

    /**
     * Registers the node exporter for the specified node types.
     * 
     * @param type The node type to register the exporter for
     * @param exporter The exporter to register, during scene export when a node with matching key is encountered this
     * exporter will be
     * called
     * @throws IllegalArgumentException If a exporter already has been registered for the type
     * @throws NullPointerException If type is null
     */
    public void registerNodeExporter(Key[] types, NodeExporter exporter);

    /**
     * Take the source node, and all children, converts to nodes that can be exported and adds to the rootnode
     * Any data needed by the node must be put in sceneData or within the source tree
     * Implementations of this method may call the {@link #exportObject(Object, RootNode)} method to export
     * data within the node.
     * 
     * @param source The source rootnode to export
     * @param rootNode The root node
     * @param rootNode
     */
    public void exportNodes(RootNode source, RootNode rootNode);

    /**
     * Exports this node, but does not export any children.
     * 
     * @param source Node to export
     * @param rootNode The root node
     * @return The Node that can be exported
     */
    public Node exportNode(Node source, RootNode rootNode);

    /**
     * Exports the object, this shall collect any data needed to export the specific object.
     * For instance used for texture objects.
     * 
     * @param object
     * @param rootNode
     */
    public void exportObject(Object object, RootNode rootNode);
}
