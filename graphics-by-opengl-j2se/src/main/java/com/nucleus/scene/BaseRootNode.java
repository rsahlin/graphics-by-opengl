package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.ResourcesData;

public class BaseRootNode extends RootNode {

    @SerializedName("resources")
    ResourcesData resources;

    @Override
    public ResourcesData getResources() {
        return resources;
    }

    @Override
    public RootNode createInstance() {
        BaseRootNode root = new BaseRootNode();
        return root;
    }

}
