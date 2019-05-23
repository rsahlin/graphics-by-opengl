package com.nucleus.scene.gltf;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.Backend.DrawMode;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.shader.GenericShaderProgram;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.ShaderProgram.ProgramType;

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

    /**
     * Used to debug TBN vectors
     */
    transient private Primitive[] debugTBNPrimitives;
    /**
     * The unresolved shader program that can be used with AssetManager to get compiled program
     */
    transient static private ShaderProgram debugTBNProgram = new GenericShaderProgram(
            new String[] { "vecline", "vecline", "vecline" }, null, ShaderProgram.Shading.flat,
            "ui", ProgramType.VERTEX_GEOMETRY_FRAGMENT);

    /**
     * Returns the array of primitives for this Mesh
     * 
     * @return
     */
    public Primitive[] getPrimitives() {
        return primitives;
    }

    /**
     * Returns the array of debug TBN primitives, or null if not created - create by calling
     * {@link #createDebugTBNPrimitives(GLES20Wrapper, Primitive[])}
     * 
     * @return
     */
    public Primitive[] getDebugTBNPrimitives() {
        return debugTBNPrimitives;
    }

    /**
     * Returns the unresolved debug tbn program, use this with AssetManager to get compiled program.
     * 
     * @return
     */
    public ShaderProgram getDebugTBNProgram() {
        return debugTBNProgram;
    }

    /**
     * Creates the primitives, and program if not already created, needed to display debug TBN vectors
     * TODO - perhaps create a DebugMesh class?
     * 
     * @param gles
     * @param primitives
     * @return
     * @throws GLException
     */
    public Primitive[] createDebugTBNPrimitives(GLES20Wrapper gles, Primitive[] primitives) throws GLException {
        Primitive[] debugTBNPrimitives = new Primitive[primitives.length];
        for (int index = 0; index < debugTBNPrimitives.length; index++) {
            Primitive p = debugTBNPrimitives[index];
            if (p == null) {
                Primitive primitive = primitives[index];
                ArrayList<Attributes> attribList = new ArrayList<>();
                ArrayList<Accessor> accessorList = new ArrayList<>();
                // Check if position accessor has offset
                Accessor position = primitive.getAccessor(Attributes.POSITION);
                accessorList.add(position);
                attribList.add(Attributes.POSITION);
                accessorList.add(primitive.getAccessor(Attributes.NORMAL));
                attribList.add(Attributes.NORMAL);
                accessorList.add(primitive.getAccessor(Attributes.TANGENT));
                attribList.add(Attributes.TANGENT);
                accessorList.add(primitive.getAccessor(Attributes.BITANGENT));
                attribList.add(Attributes.BITANGENT);
                // Create the primitive used to draw vector lines
                p = new Primitive(attribList, accessorList, primitive.getIndices(),
                        new Material(), DrawMode.POINTS);
                debugTBNPrimitives[index] = p;
            }
        }
        return debugTBNPrimitives;
    }

    /**
     * Returns the optional weights for morph targets
     * 
     * @return
     */
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
