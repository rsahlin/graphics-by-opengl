package com.nucleus.exporter;

import java.util.HashMap;
import java.util.List;

import com.nucleus.assets.AssetManager;
import com.nucleus.common.Key;
import com.nucleus.geometry.Mesh;
import com.nucleus.scene.Node;
import com.nucleus.scene.SceneData;
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
    public Node exportNodes(Node source, SceneData sceneData) {
        NodeExporter exporter = nodeExporters.get(source.getType());
        Node export = null;
        if (exporter != null) {
            export = exporter.exportNode(source, sceneData);
        } else {
            export = new Node(source);
        }
        for (Node child : source.getChildren()) {
            export.addChild(exportNodes(child, sceneData));
        }
        return export;
    }

    @Override
    public void exportObject(Object object, SceneData sceneData) {
        // TODO Auto-generated method stub

    }

    /**
     * Exports a mesh to scenedata
     * This will currently only export the texture(s)
     * 
     * @param mesh
     * @param sceneData
     */
    protected void exportMesh(Mesh mesh, SceneData sceneData) {
        exportTextures(mesh.getTextures(), sceneData);
    }

    /**
     * Exports the meshes to scenedata
     * 
     * 
     * @param meshes
     * @param sceneData
     */
    protected void exportMeshes(List<Mesh> meshes, SceneData sceneData) {
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
    protected void exportTextures(Texture2D[] texture, SceneData sceneData) {
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
    protected void exportTexture(Texture2D texture, SceneData sceneData) {
        texture.setExternalReference(AssetManager.getInstance().getSourceReference(texture.getId()));
        sceneData.addResource(texture);
    }

    @Override
    public Node exportNode(Node source, SceneData sceneData) {
        return new Node(source);
    }

}
