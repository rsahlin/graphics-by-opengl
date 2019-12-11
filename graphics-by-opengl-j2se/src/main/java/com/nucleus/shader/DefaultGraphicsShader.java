package com.nucleus.shader;

import java.nio.FloatBuffer;

import com.nucleus.Backend;
import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.SimpleLogger;
import com.nucleus.common.BufferUtils;
import com.nucleus.environment.Lights;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.vecmath.Matrix;

/**
 * Shared class for shaderprograms - regardless of platform implementation (GL/Vulkan)
 *
 */
public abstract class DefaultGraphicsShader implements GraphicsShader {

    private final static String NO_ACTIVE_UNIFORMS = "No active uniforms, forgot to call createProgram()?";

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

    protected GenericShaderProgram.ProgramType shaders;
    protected GraphicsPipeline<?> pipeline;

    protected ShaderVariable modelUniform;

    /**
     * Must be called before the shader is used.
     * 
     * @param function
     * @param shaders
     */
    protected void init(Categorizer function, GenericShaderProgram.ProgramType shaders) {
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

    @Override
    public void setUniformMatrices(float[][] matrices) {
        // Refresh the uniform matrixes - default is model - view and projection
        if (modelUniform == null) {
            modelUniform = getUniformByName(Matrices.Name);
        }
        FloatBuffer uniforms = getUniformData();
        uniforms.position(modelUniform.getOffset());
        uniforms.put(matrices[Matrices.MODEL.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.put(matrices[Matrices.VIEW.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.put(matrices[Matrices.PROJECTION.index], 0, Matrix.MATRIX_ELEMENTS);
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
    public void setSamplers(ShaderVariable[] activeUniforms) {
        if (activeUniforms == null) {
            throw new IllegalArgumentException(NO_ACTIVE_UNIFORMS);
        }
        int unit = 0;
        for (ShaderVariable var : activeUniforms) {
            switch (var.getDataType()) {
                case GLES20.GL_SAMPLER_2D:
                case GLES20.GL_SAMPLER_CUBE:
                case GLES30.GL_SAMPLER_2D_SHADOW:
                case GLES30.GL_SAMPLER_2D_ARRAY:
                case GLES30.GL_SAMPLER_2D_ARRAY_SHADOW:
                case GLES30.GL_SAMPLER_CUBE_SHADOW:
                case GLES30.GL_SAMPLER_3D:
                    uniforms.position(var.getOffset());
                    for (int i = 0; i < var.getSize(); i++) {
                        uniforms.put(unit++);
                    }
                default:
                    // Do nothing.
            }
        }
        if (unit > 0) {
            SimpleLogger.d(getClass(), "Stored " + unit + " texture units.");
        } else {
            SimpleLogger.d(getClass(), "No texture units.");
        }
    }

    @Override
    public void initShader(GraphicsPipeline<?> pipeline) {
        this.pipeline = pipeline;
        createUniformBuffer(pipeline.getVariableSize(VariableType.UNIFORM));
        setSamplers(pipeline.getActiveVariables(VariableType.UNIFORM));
        initUniformData();
    }

    @Override
    public GraphicsPipeline<?> getPipeline() {
        return pipeline;
    }

    @Override
    public void updateUniformData() {
    }

    @Override
    public void initUniformData() {
    }

    @Override
    public void uploadUniforms() throws BackendException {
        pipeline.uploadUniforms(uniforms, null);
    }

}
