package com.nucleus.scene.gltf;

/**
 * Runtime instance of a glTF mesh that can be rendered, without reference to main asset
 *
 */
public class RenderableMesh extends Mesh {

    protected GLTF asset;

    public RenderableMesh(GLTF asset) {
        this.asset = asset;
    }

}
