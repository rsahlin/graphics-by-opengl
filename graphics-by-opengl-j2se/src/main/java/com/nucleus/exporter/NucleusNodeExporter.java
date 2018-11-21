package com.nucleus.exporter;

import java.util.HashMap;
import java.util.List;

import com.nucleus.common.Type;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.RootNodeImpl;

public class NucleusNodeExporter implements NodeExporter {

    public final static String NOT_IMPLEMENTED = "Not implemented: ";

    /**
     * Register node exporters by node type, when a node of registered type shall be serialized the exporter is called
     * to prepare the node to be serialized, for instance collecting additional data.
     */
    private HashMap<String, NodeExporter> nodeExporters = new HashMap<String, NodeExporter>();

    @Override
    public void registerNodeExporter(Type<Node> type, NodeExporter exporter) {
        if (nodeExporters.containsKey(type.getName())) {
            throw new IllegalArgumentException(ALREADY_REGISTERED_TYPE + type.getName());
        }
        nodeExporters.put(type.getName(), exporter);
    }

    @Override
    public void exportNodes(RootNode source, RootNode rootNode) {
        List<Node> children = source.getChildren();
        for (Node node : children) {
            NodeExporter exporter = nodeExporters.get(node.getType());
            Node export = null;
            if (exporter != null) {
                export = exporter.exportNode(node, rootNode);
                rootNode.addChild(export);
            } else {
                throw new IllegalAccessError("Invalid node type: " + node.getType());
            }
            for (Node child : node.getChildren()) {
                NodeExporter exporter2 = nodeExporters.get(child.getType());
                // export.addChild(exporter2.exportNode(child, rootNode));
            }

        }
    }

    @Override
    public void exportObject(Object object, RootNodeImpl rootNode) {
        // TODO Auto-generated method stub

    }

    @Override
    public Node exportNode(Node source, RootNode rootNode) {
        return source.createInstance(rootNode);
    }

    @Override
    public void registerNodeExporter(Type<Node>[] types, NodeExporter exporter) {
        for (Type<Node> t : types) {
            registerNodeExporter(t, exporter);
        }
    }

}
