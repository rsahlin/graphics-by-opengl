package com.nucleus.geometry;

import com.nucleus.geometry.Mesh.Builder;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Texture2D;

/**
 * Utility class to help build different type of Meshes.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class MeshBuilder<T> {

    /**
     * Create a new empty instance of a mesh.
     * 
     * @return
     */
    protected abstract T createMesh();

    /**
     * Creates a Builder to create mesh.
     * 
     * @param renderer
     * @param maxVerticeCount
     * @param material
     * @param program
     * @param texture
     * @param shapeBuilder
     * @param mode
     * @return
     */
    public static Builder<Mesh> createBuilder(NucleusRenderer renderer, int maxVerticeCount, Material material,
            ShaderProgram program,
            Texture2D texture, ShapeBuilder shapeBuilder, Mesh.Mode mode) {
        Mesh.Builder<Mesh> builder = new Mesh.Builder<>(renderer);
        material.setProgram(program);
        builder.setTexture(texture);
        builder.setMaterial(material);
        builder.setArrayMode(mode, maxVerticeCount);
        builder.setShapeBuilder(shapeBuilder);
        return builder;
    }
}
