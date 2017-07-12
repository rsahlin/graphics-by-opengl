package com.nucleus.exporter;

import java.util.HashMap;

import com.nucleus.common.Key;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeType;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.SwitchNode;

public class NucleusNodeExporter implements NodeExporter {

    public final static String NOT_IMPLEMENTED = "Not implemented: ";

    /**
     * Register node exporters by node type, when a node of registered type shall be serialized the exporter is called
     * to prepare the node to be serialized, for instance collecting additional data.
     */
    private HashMap<String, NodeExporter> nodeExporters = new HashMap<String, NodeExporter>();

    @Override
    public void registerNodeExporter(Key type, NodeExporter exporter) {
        if (nodeExporters.containsKey(type.getKey())) {
            throw new IllegalArgumentException(ALREADY_REGISTERED_TYPE + type.getKey());
        }
        nodeExporters.put(type.getKey(), exporter);
    }

    @Override
    public void exportNodes(RootNode source, RootNode rootNode) {
        Node scene = source.getScene();
        NodeExporter exporter = nodeExporters.get(scene.getType());
        Node export = null;
        if (exporter != null) {
            export = exporter.exportNode(scene, rootNode);
            rootNode.setScene(export);
        } else {
            throw new IllegalAccessError("Invalid node type: " + scene.getType());
        }
        for (Node child : scene.getChildren()) {
            NodeExporter exporter2 = nodeExporters.get(child.getType());
            // export.addChild(exporter2.exportNode(child, rootNode));
        }
    }

    @Override
    public void exportObject(Object object, RootNode rootNode) {
        // TODO Auto-generated method stub

    }


    @Override
    public Node exportNode(Node source, RootNode rootNode) {
        NodeType type = NodeType.valueOf(source.getType());
        Node created;
        switch (type) {
        case node:
            created = source.copy(rootNode);
            break;
        case layernode:
            created = ((LayerNode) source).copy(rootNode);
            break;
        case switchnode:
            created = ((SwitchNode) source).copy(rootNode);
            break;
        default:
            throw new IllegalArgumentException(NOT_IMPLEMENTED + type);
        }
        created.setRootNode(rootNode);
        return created;
    }

    @Override
    public void registerNodeExporter(Key[] types, NodeExporter exporter) {
        for (Key k : types) {
            registerNodeExporter(k, exporter);
        }
    }

}
