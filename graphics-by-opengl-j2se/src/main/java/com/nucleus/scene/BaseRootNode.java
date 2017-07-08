package com.nucleus.scene;

public class BaseRootNode extends RootNode {


    @Override
    public RootNode createInstance() {
        BaseRootNode root = new BaseRootNode();
        return root;
    }

}
