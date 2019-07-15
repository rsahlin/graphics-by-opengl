package com.nucleus.shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.nucleus.environment.Lights;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.opengl.shader.GLShaderProgram;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;

/**
 * Shared class for shaderprograms - regardless of platform implementation (GL/Vulkan)
 *
 */
public abstract class ShaderProgram implements GraphicsShader {

    /**
     * The basic function
     */
    protected Categorizer function;

    protected Lights globalLight = Lights.getInstance();

    /**
     * Uniforms, used when rendering - uniforms array shall belong to program since uniforms are a property of the
     * program. This data is quite small and the size depends on what program is used - and not the mesh.
     * The same mesh may be rendered with different programs, for instance different shadow passes and will have
     * different number of uniforms depending on the program.
     * 
     */
    transient protected FloatBuffer uniforms;

    /**
     * Calculated in create program, created using {@link #attributeBufferCount}
     * If attributes are dynamically mapped (not using indexer) then only one buffer is used.
     */
    transient protected ShaderVariable[][] attributeVariables;

    protected BufferIndex defaultDynamicAttribBuffer = BufferIndex.ATTRIBUTES_STATIC;

    protected int attributeBufferCount = BufferIndex.values().length;
    /**
     * If specified then variable offsets will be taken from this.
     */
    protected VariableIndexer variableIndexer;
    /**
     * Read when shader source is created in {@link #createShaderSource(Renderers)}
     * Subclasses may modify before {@link #createProgram(NucleusRenderer)} is called - or before they call
     * super.createProgram()
     */
    protected GLShaderProgram.ProgramType shaders;

    /**
     * Uniform interface blocks
     */
    protected InterfaceBlock[] uniformInterfaceBlocks;
    protected BlockBuffer[] uniformBlockBuffers;
    /**
     * Samplers (texture units) - the texture unit to use for a shadervariable is stored at the intbuffer
     * position. To fetch texture unit to use for a shadervariable do: samplers.position(shadervariable.position())
     */
    transient protected IntBuffer samplers;

    protected ShaderVariable modelUniform;

    /**
     * The size of each buffer for the attribute variables - as set either from indexer if this is used or taken
     * from defined attributes.
     */
    protected int[] attributesPerVertex;
    /**
     * Optional additional storage per vertex, used when attribute buffer is created.
     */
    protected int[] paddingPerVertex;

    protected ShaderProgram(Pass pass, Shading shading, String category, GLShaderProgram.ProgramType shaders) {
        function = new Categorizer(pass, shading, category);
        this.shaders = shaders;
    }

    protected ShaderProgram(Categorizer function, GLShaderProgram.ProgramType shaders) {
        this.function = function;
        this.shaders = shaders;
    }

    @Override
    public FloatBuffer getUniformData() {
        return uniforms;
    }

    /**
     * Sets the float values from data at the offset from variable, use this to set more than one value.
     * 
     * @param variable The shader variable to set uniform data to
     * @param data The uniform data to set
     * @param sourceOffset Offset into data where values are read
     */
    public void setUniformData(ShaderVariable variable, float[] data, int sourceOffset) {
        uniforms.position(variable.getOffset());
        uniforms.put(data, sourceOffset, variable.getSizeInFloats());
    }

    @Override
    public String getKey() {
        return getClass().getSimpleName() + function.getShadingString() + function.getCategoryString();
    }

    @Override
    public void setIndexer(VariableIndexer variableIndexer) {
        this.variableIndexer = variableIndexer;
    }

}
