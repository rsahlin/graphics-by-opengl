package com.nucleus.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;

/**
 * Create a scene node without a direct connection to the underlying implementation of how to load and parse
 * scene data.
 * 
 * @author Richard Sahlin
 *
 */
public interface SceneSerializer {

    public final static String NULL_RENDERER_ERROR = "Renderer is null.";
    public final static String RENDERER_NOT_SET_ERROR = "Renderer is not set, must set before calling this method.";

    /**
     * Sets the renderer needed when scenes are imported.
     * This method must be called before importScene is called.
     * 
     * @param renderer
     * @throws IllegalArgumentException If renderer is null
     */
    public void setRenderer(NucleusRenderer renderer);

    /**
     * Creates a named node from a scene, the scene will be loaded using filename and the node returned shall be named
     * name.
     * Before calling this method the renderer must be set, otherwise loading of texture and materials cannot be
     * created.
     * 
     * @param filename Name of file containing scene data.
     * @param name Name of scene to create
     * @return The scene node with matching name, including all defined children.
     * @throws IOException If there is an exception loading the data.
     * @throws IllegalStateException If the renderer has not been set before calling this method.
     */
    public Node importScene(String filename, String name) throws IOException;

    /**
     * Creates a named node from a scene, the scene will be loaded from the inputstream.
     * Same as calling {@link #importScene(String, String)} but with stream instead of filename.
     * 
     * @param is Inputstream containing the scene data
     * @param name Name of scene to create
     * @return The scene node with matching name, including all defined children.
     * @throws IOException If there is an exception loading the data.
     */
    public Node importScene(InputStream is, String name) throws IOException;

    /**
     * Exports a scene in the same format as this serializer can import.
     * 
     * @param out
     * @param obj
     * @throws IOException
     */
    public void exportScene(OutputStream out, Object obj) throws IOException;

}
