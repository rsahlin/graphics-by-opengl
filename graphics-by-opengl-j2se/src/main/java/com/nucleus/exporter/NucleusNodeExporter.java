package com.nucleus.exporter;

import java.util.HashMap;
import java.util.List;

import com.nucleus.assets.AssetManager;
import com.nucleus.common.Key;
import com.nucleus.geometry.Mesh;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeType;
import com.nucleus.scene.RootNode;
import com.nucleus.texturing.Texture2D;

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
        for (Node n : source.getScenes()) {
            NodeExporter exporter = nodeExporters.get(n.getType());
            Node export = null;
            if (exporter != null) {
                export = exporter.exportNode(n, rootNode);
            } else {
                throw new IllegalAccessError("Invalid node type: " + n.getType());
            }
            for (Node child : n.getChildren()) {
                export.addChild(exportNode(child, rootNode));
            }
        }
    }

    @Override
    public void exportObject(Object object, RootNode rootNode) {
        // TODO Auto-generated method stub

    }

    /**
     * Exports a mesh to scenedata
     * This will currently only export the texture(s)
     * 
     * @param mesh
     * @param sceneData
     */
    protected void exportMesh(Mesh mesh, RootNode sceneData) {
        exportTextures(mesh.getTextures(), sceneData);
    }

    /**
     * Exports the meshes to scenedata
     * 
     * 
     * @param meshes
     * @param sceneData
     */
    protected void exportMeshes(List<Mesh> meshes, RootNode sceneData) {
        for (Mesh mesh : meshes) {
            exportMesh(mesh, sceneData);
        }
    }

    /**
     * Exports the textures texture to scenedata, this will lookup the external reference for the specified texture
     * and add the texture to scenedata.
     * 
     * @param texture
     * @param sceneData
     */
    protected void exportTextures(Texture2D[] texture, RootNode sceneData) {
        for (Texture2D t : texture) {
            exportTexture(t, sceneData);
        }

    }

    /**
     * Exports the textures texture to scenedata, this will lookup the external reference for the specified texture
     * and add the texture to scenedata.
     * 
     * @param texture The texture to export, will be added to scenedata after the external reference has been set.
     * @param sceneData
     */
    protected void exportTexture(Texture2D texture, RootNode sceneData) {
        texture.setExternalReference(AssetManager.getInstance().getSourceReference(texture.getId()));
        sceneData.addResource(texture);
    }

    @Override
    public Node exportNode(Node source, RootNode rootNode) {
        NodeType type = NodeType.valueOf(source.getType());
        Node created;
        switch (type) {
        case node:
            created = source.copy();
            break;
        case layernode:
            created = source.copy();
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
