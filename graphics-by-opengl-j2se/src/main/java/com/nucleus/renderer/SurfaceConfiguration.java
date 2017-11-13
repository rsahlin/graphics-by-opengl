package com.nucleus.renderer;

import java.util.ArrayList;
import java.util.List;

import com.nucleus.common.StringUtils;

/**
 * Class that specifies the surface configuration, bitdepth, zbufferm samples and other
 * surface specific configurations.
 * Use with the @link Renderer
 * 
 * @author Richard Sahlin
 */
public class SurfaceConfiguration {

    public final static int DEFAULT_RED_BITS = 8;
    public final static int DEFAULT_GREEN_BITS = 8;
    public final static int DEFAULT_BLUE_BITS = 8;
    public final static int DEFAULT_ALPHA_BITS = 8;
    public final static int DEFAULT_DEPTH_BITS = 16;
    public final static int DEFAULT_SAMPLES = 0;

    protected final static String INVALID_SAMPLES_STRING = "Invalid samples value";

    /**
     * The display red bit (colour) depth
     */
    protected int redBits = DEFAULT_RED_BITS;
    /**
     * The display green bit (colour) depth
     */
    protected int greenBits = DEFAULT_GREEN_BITS;
    /**
     * The display blue bit (colour) depth
     */
    protected int blueBits = DEFAULT_BLUE_BITS;

    /**
     * Number of bits to use for alpha.
     */
    protected int alphaBits = DEFAULT_ALPHA_BITS;

    /**
     * Number of bits to use for the depth buffer.
     */
    protected int depthBits = DEFAULT_DEPTH_BITS;

    /**
     * Number of samples to require in SampleBuffers.
     */
    protected int samples = DEFAULT_SAMPLES;

    /**
     * EGL version
     */
    protected String version;
    protected String vendor;
    protected List<String> extensions = new ArrayList<String>();

    /**
     * Constructs a SurfaceConfiguration that can used when
     * selecting EGL configuration.
     * All parameters are set to default values.
     */
    public SurfaceConfiguration() {
    }

    /**
     *
     * @param redBits Number of red bits
     * @param greenBits Number of green bits
     * @param blueBits Number of blue bits
     * @param alphaBits Number of alpha bits
     * @param depthBits Number of depth bits
     * @param samples The number of samples for each pixel.
     */
    public SurfaceConfiguration(int redBits, int greenBits, int blueBits, int alphaBits, int depthBits, int samples) {
        this.redBits = redBits;
        this.greenBits = greenBits;
        this.blueBits = blueBits;
        this.alphaBits = alphaBits;
        this.depthBits = depthBits;
        this.samples = samples;
    }

    /**
     * Return the number of bits to use for red.
     * 
     * @return Number of bits to use for red.
     */
    public int getRedBits() {
        return redBits;
    }

    /**
     * Return the number of bits to use for green.
     * 
     * @return Number of bits to use for green.
     */
    public int getGreenBits() {
        return greenBits;
    }

    /**
     * Return the number of bits to use for blue.
     * 
     * @return Number of bits to use for blue.
     */
    public int getBlueBits() {
        return blueBits;
    }

    /**
     * Return the number of bits to use for alpha.
     * 
     * @return Number of alpha bits.
     */
    public int getAlphaBits() {
        return alphaBits;
    }

    /**
     * Return the number of bits to use for depth buffer.
     *
     * @return Number of bits to use in depth buffer.
     */
    public int getDepthBits() {
        return depthBits;
    }

    /**
     * Return the number of samples required for this configuration.
     * 
     * @return The number of samples required for this configuration.
     */
    public int getSamples() {
        return samples;
    }

    /**
     * Sets the wanted number of samples for the EGL buffer.
     * 
     * @param samples The number of samples
     * @throws IllegalArgumentException If samples is negative
     */
    public void setSamples(int samples) {
        if (samples < 0) {
            throw new IllegalArgumentException(INVALID_SAMPLES_STRING);
        }
        this.samples = samples;
    }

    /**
     * Sets the number of wanted redbits, at least this value - may get a config with more.
     * 
     * @param redbits
     */
    public void setRedBits(int redbits) {
        redBits = redbits;
    }

    /**
     * Sets the number of wanted greenbits, at least this value - may get a config with more.
     * 
     * @param greenbits
     */
    public void setGreenBits(int greenbits) {
        greenBits = greenbits;
    }

    /**
     * Sets the number of wanted bluebits, at least this value - may get a config with more.
     * 
     * @param bluebits
     */
    public void setBlueBits(int bluebits) {
        blueBits = bluebits;
    }

    /**
     * Sets the number of wanted alphabits, at least this value - may get a config with more.
     * 
     * @param alphabits
     */
    public void setAlphaBits(int alphabits) {
        alphaBits = alphabits;
    }

    /**
     * Sets the number of wanted depthbits, at least this value - may get a config with more.
     * @param depthbits
     */
    public void setDepthBits(int depthbits){
        depthBits = depthbits;
    }

    @Override
    public String toString() {
        return "RGBA:" + redBits + ", " + greenBits + ", " + blueBits + ", " + alphaBits + ", Depth: " + depthBits
                + ", Samples: " + samples + ", Version: " + version + ", Vendor: " + vendor;
    }

    /**
     * Sets the egl infp
     * 
     * @param version
     * @param vendor
     * @param extensions
     */
    public void setInfo(String version, String vendor, String extensions) {
        this.version = version;
        this.vendor = vendor;
        this.extensions = StringUtils.getList(extensions, " ");
    }

    /**
     * Returns the EGL version.
     * 
     * @return
     */
    public String getVersion() {
        return version;
    }

}
