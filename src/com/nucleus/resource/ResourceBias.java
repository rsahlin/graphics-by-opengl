package com.nucleus.resource;

/**
 * Help class for selecting what resource size to load or scale.
 * 
 * @author Richard Sahlin
 *
 */
public class ResourceBias {

    public enum RESOLUTION {
        TWO_FORTY(240),
        THREE_TWENTY(320),
        FOUR_EIGHTY(480),
        FIVE_FORTY(540),
        SEVEN_TWENTY(720),
        HD(1080),
        ULTRA_HD(2160);

        public final int lines;

        private RESOLUTION(int lines) {
            this.lines = lines;
        }

    }

    /**
     * Returns the scale factor for a given width/height compared to the base height.
     * Width and height will be checked for landscape orientation (width > height) and swapped
     * if necessary.
     * 
     * @param width
     * @param height
     * @param baseHeight The target height for the image, ie the resolution the image is inteded for
     * @return Scale factor for image
     */
    public static float getScaleFactorLandscape(int width, int height, int baseHeight) {
        if (width < height) {
            return getScaleFactorLandscape(height, width, baseHeight);
        }
        return (float) height / baseHeight;
    }

}
