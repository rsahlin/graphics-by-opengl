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
     * @param name The filename to load
     * @param scaleX If image should be scaled in X, 1 = original size, 1.5 = 50% bigger
     * @param scaleY If image should be scaled in Y, 1 = original size, 1.5 = 50% bigger
     * @return The loaded image.
     * @throws IOException If there is an error loading the image.
     */
    public Image createImage(String name, float scaleX, float scaleY) throws IOException;

}
