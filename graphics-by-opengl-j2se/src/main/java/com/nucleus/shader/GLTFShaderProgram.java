package com.nucleus.shader;

import com.nucleus.renderer.Pass;
import com.nucleus.scene.gltf.AccessorDictionary;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.texturing.Texture2D.Shading;

public class GLTFShaderProgram extends GenericShaderProgram {

    /**
     * The dictionary created from linked program
     */
    protected AccessorDictionary<String> accessorDictionary = new AccessorDictionary<>();

    protected Mesh[] meshes;

    public GLTFShaderProgram(Mesh[] meshes, Pass pass, Shading shading, String category, ProgramType shaders) {
        super(pass, shading, category, shaders);
        this.meshes = meshes;
    }

    @Override
    protected void addShaderVariable(ShaderVariable variable) {
        super.addShaderVariable(variable);
        if (variable.getType() == VariableType.ATTRIBUTE) {
            // createAccessor(variable);
        }
    }

    /**
     * Creates the accessor, using shader variable name as key.
     * 
     * @param variable
     */
    protected void createAccessor(ShaderVariable variable) {
    }

    /**
     * Returns the program accessor dictionary, this is created after linking the program and stores accessors
     * using shader variable name.
     * 
     * @return
     */
    public AccessorDictionary<String> getAccessorDictionary() {
        return accessorDictionary;
    }

}
