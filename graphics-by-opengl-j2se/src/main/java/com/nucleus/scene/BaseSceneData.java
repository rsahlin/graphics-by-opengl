package com.nucleus.scene;

import com.nucleus.geometry.Mesh;
import com.nucleus.texturing.Texture2D;

public class BaseSceneData extends SceneData {

    ResourcesData resources;

    /**
     * Returns the resources in this scene, this is the objects that are used to make up the nodes.
     * 
     * @return
     */
    public ResourcesData getResources() {
        return resources;
    }

    @Override
    public void addResource(Texture2D texture) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void addResource(Mesh mesh) {
        throw new IllegalArgumentException("Not implemented");
    }

}
