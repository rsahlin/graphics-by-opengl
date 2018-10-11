package com.nucleus.scene.gltf;

import com.nucleus.scene.GLTFNode;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.RootNodeImpl;

/**
 * Used for loading gltf scenes into rootnode, ie when the whole nodetree shall be gltf.
 * If a gltf scene/model shall be loaded into an existing nucleus scene then use {@link GLTFNode}
 * 
 *
 */
public class GLTFRootNode extends RootNodeImpl {

    @Override
    public RootNode createInstance() {
        GLTFRootNode copy = new GLTFRootNode();
        copy.copy(this);
        return copy;
    }

}
