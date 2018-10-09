package com.nucleus.scene;

/**
 * Used for loading gltf scenes into rootnode, ie when the whole nodetree shall be gltf.
 * If a gltf scene/model shall be loaded into an existing nucleus scene then use {@link GLTFNode}
 * 
 *
 */
public class GLTFRootNode extends RootNode {

    @Override
    public Node createInstance(RootNode root) {
        return new GLTFRootNode();
    }

    @Override
    public void createTransient() {
    }

}
