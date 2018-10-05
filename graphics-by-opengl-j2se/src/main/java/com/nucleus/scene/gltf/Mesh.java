package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;

/**
 * 
 * The Mesh as it is loaded using the glTF format.
 * 
 * mesh
 * A set of primitives to be rendered. A node can contain one mesh. A node's transform places the mesh in the scene.
 * 
 * Properties
 * 
 * Type Description Required
 * primitives primitive [1-*] An array of primitives, each defining geometry to be rendered with a material. ✅ Yes
 * weights number [1-*] Array of weights to be applied to the Morph Targets. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 * 
 *
 */
public class Mesh extends GLTFNamedValue implements AttributeUpdater, RuntimeResolver {

    private static final String PRIMITIVES = "primitives";
    private static final String WEIGHTS = "weights";

    @SerializedName(PRIMITIVES)
    private Primitive[] primitives;
    @SerializedName(WEIGHTS)
    private int[] weights;

    public Primitive[] getPrimitives() {
        return primitives;
    }

    public int[] getWeights() {
        return weights;
    }

    @Override
    public AttributeBuffer getAttributeBuffer(BufferIndex buffer) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AttributeBuffer getAttributeBuffer(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void destroy(NucleusRenderer renderer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAttributeUpdater(Consumer attributeConsumer) {
        // TODO Auto-generated method stub

    }

    @Override
    public Consumer getAttributeConsumer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void resolve(GLTF asset) throws GLTFException {
        if (primitives != null) {
            for (Primitive p : primitives) {
                p.resolve(asset);
            }
        }

    }

}
