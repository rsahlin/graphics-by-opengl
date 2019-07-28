package com.nucleus.shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.nucleus.Backend;
import com.nucleus.GraphicsPipeline;
import com.nucleus.common.BufferUtils;
import com.nucleus.environment.Lights;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.vecmath.Matrix;

/**
 * Shared class for shaderprograms - regardless of platform implementation (GL/Vulkan)
 *
 */
public abstract class DefaultGraphicsShader implements GraphicsShader {

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
    protected GenericShaderProgram.ProgramType shaders;
    protected GraphicsPipeline<?> pipeline;

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

    /**
     * The size of each buffer for the attribute variables - as set either from indexer if this is used or taken
     * from defined attributes.
     */
    protected int[] attributesPerVertex;
    /**
     * Optional additional storage per vertex, used when attribute buffer is created.
     */
    protected int[] paddingPerVertex;
    protected ShaderVariable modelUniform;

    protected DefaultGraphicsShader(Pass pass, Shading shading, String category,
            GenericShaderProgram.ProgramType shaders) {
        function = new Categorizer(pass, shading, category);
        this.shaders = shaders;
        setLibNames();
    }

    protected DefaultGraphicsShader(Categorizer function, GenericShaderProgram.ProgramType shaders) {
        this.function = function;
        this.shaders = shaders;
        setLibNames();
    }

    /**
     * Called by constructor(s) - subclasses must set the library names in the function categorizer.
     */
    protected void setLibNames() {
        for (ShaderType st : ShaderType.values()) {
            String[] names = getLibName(Backend.getInstance().getVersion(), st);
            if (names != null) {
                function.addLibNames(st, names);
            }
        }
    }

    /**
     * Returns the common (library) shader name for the specified type.
     * Override to include needed files.
     * 
     * @param version
     * @param type
     * @return
     */
    protected String[] getLibName(Renderers version, ShaderType type) {
        // Default is to not use common library
        return null;
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
    @Override
    public void setUniformData(NamedShaderVariable variable, float[] data, int sourceOffset) {
        uniforms.position(variable.getOffset());
        uniforms.put(data, sourceOffset, variable.getSizeInFloats());
    }

    @Override
    public String getKey() {
        return getClass().getSimpleName() + function.getShadingString() + function.getCategoryString();
    }

    @Override
    public void setIndexer(VariableIndexer variableIndexer) {
        this.function.indexer = variableIndexer;
    }

    @Override
    public ProgramType getType() {
        return shaders;
    }

    @Override
    public Categorizer getFunction() {
        return function;
    }

    @Override
    public void createUniformBuffer(int floatSize) {
        uniforms = BufferUtils.createFloatBuffer(floatSize);
    }

    /**
     * 
     * Sets the data for the uniform matrices needed by the program - the default implementation will set the modelview
     * and projection matrices. Will NOT set uniforms to backend api, only update the uniform array store
     * 
     * @param matrices Source matrices to set to uniform data array.
     */
    public void setUniformMatrices(float[][] matrices, ShaderVariable modelUniform) {
        // Refresh the uniform matrixes - default is model - view and projection
        if (modelUniform == null) {
            // modelUniform = getUniformByName(Matrices.Name);
        }
        FloatBuffer uniforms = getUniformData();
        uniforms.position(modelUniform.getOffset());
        uniforms.put(matrices[Matrices.MODEL.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.put(matrices[Matrices.VIEW.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.put(matrices[Matrices.PROJECTION.index], 0, Matrix.MATRIX_ELEMENTS);
    }

    /**
     * Returns the indexer used when creating this program, or null if not set.
     * 
     * @return
     */
    public VariableIndexer getIndexer() {
        return variableIndexer;
    }

    public NamedShaderVariable getUniformByName(String uniform) {
        return getVariableByName(uniform, (NamedShaderVariable[]) pipeline.getActiveVariables(VariableType.UNIFORM));
    }

    public NamedShaderVariable getAttributeByName(String attribute) {
        return getVariableByName(attribute,
                (NamedShaderVariable[]) pipeline.getActiveVariables(VariableType.ATTRIBUTE));
    }

    protected NamedShaderVariable getVariableByName(String name, NamedShaderVariable[] variables) {
        for (NamedShaderVariable v : variables) {
            if (v != null && v.getName().contentEquals(name)) {
                return v;
            }
        }
        return null;
    }

    @Override
    public void initShader(GraphicsPipeline<?> pipeline) {
        this.pipeline = pipeline;
        createUniformBuffer(pipeline.getVariableSize(VariableType.UNIFORM));
        initUniformData();
    }

    @Override
    public GraphicsPipeline<?> getPipeline() {
        return pipeline;
    }

}
