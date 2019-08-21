package com.nucleus.shader;

import java.nio.FloatBuffer;

import com.nucleus.SimpleLogger;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.Window;
import com.nucleus.texturing.TiledTexture2D;

/**
 * Generic shader program - use this when a specific shader source shall be specified.
 * Loads from /assets folder and appends 'vertex', 'fragment', 'geometry' after source name.
 *
 */
public class GenericShaderProgram extends DefaultGraphicsShader {

    /**
     * Inits a shader program that will load shaders from default location
     * 
     * @param source Source names for shaders, must match number of shader types in Shader.
     * @param pass
     * @param shading
     * @param category
     * @param shaders
     * {@link ProgramType#VERTEX_FRAGMENT} then this must contain 2 values.
     * @param shaders
     */
    public void init(String[] source, Pass pass, Shading shading, String category,
            GenericShaderProgram.ProgramType shaders) {
        super.init(new Categorizer(pass, shading, category), shaders);
        function.sourceNames = source;
    }

    /**
     * Sets UV fraction for the tiled texture + number of frames in x.
     * Use this for programs that use tiled texture behavior.
     * 
     * @param texture
     * @param uniforms Will store 1 / tilewidth, 1 / tilewidth, tilewidth, beginning at offset
     * @param variable The shader variable
     * @param offset Offset into destination where fraction is set
     */
    protected void setTextureUniforms(TiledTexture2D texture, FloatBuffer uniforms, ShaderVariable variable) {
        if (texture.getWidth() == 0 || texture.getHeight() == 0) {
            SimpleLogger.d(getClass(), "ERROR! Texture size is 0: " + texture.getWidth() + ", " + texture.getHeight());
        }
        uniforms.position(variable.getOffset());
        uniforms.put((((float) texture.getWidth()) / texture.getTileWidth()) / (texture.getWidth()));
        uniforms.put((((float) texture.getHeight()) / texture.getTileHeight()) / (texture.getHeight()));
        uniforms.put(texture.getTileWidth());
    }

    /**
     * Sets the screensize to uniform storage
     * 
     * @param uniforms
     * @param uniformScreenSize
     */
    protected void setScreenSize(FloatBuffer uniforms, ShaderVariable uniformScreenSize) {
        if (uniformScreenSize != null) {
            uniforms.position(uniformScreenSize.getOffset());
            uniforms.put(Window.getInstance().getWidth());
            uniforms.put(Window.getInstance().getHeight());
        }
    }

}
