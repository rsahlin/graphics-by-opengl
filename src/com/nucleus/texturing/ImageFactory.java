package com.nucleus.texturing;

import java.io.IOException;

/**
 * Platform abstraction for creating images.
 * 
 * @author Richard Sahlin
 *
 */
public interface ImageFactory {

    /**
     * Loads an image, the image must be in a format that is understood by the platform.
     * 
     * @return The image The loaded image.
     * @throws IOException If there is an error loading the image.
     */
    public Image createImage(String name) throws IOException;

}
