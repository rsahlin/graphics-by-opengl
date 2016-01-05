package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.ResourcesData;

public class BaseSceneData extends SceneData {

    @SerializedName("resources")
    ResourcesData resources;

    @Override
    public ResourcesData getResources() {
        return resources;
    }

}
