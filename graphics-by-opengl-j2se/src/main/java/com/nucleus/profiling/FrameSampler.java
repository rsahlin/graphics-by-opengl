package com.nucleus.profiling;

import com.nucleus.SimpleLogger;
import com.nucleus.texturing.Image;

/**
 * Utility class for keeping track of delta times, normally used to calculate the delta time from one frame to the next.
 * Singleton class that can be
 * 
 * @author Richard Sahlin
 *
 */
public class FrameSampler {

    public final static String DISPLAY_SPLASH = "DISPLAY_SPLASH";
    public final static String SET_ROOT_NODE = "SET_ROOT_NODE";
    public final static String LOAD_SCENE = "LOAD_SCENE";
    public final static String CREATE_SCENE = "CREATE_SCENE";
    public final static String CREATE_NODE = "CREATE_NODE";
    public final static String LOAD_MAP = "LOAD_MAP";
    public final static String CREATE_SHADER = "CREATE_SHADER";
    /**
     * The whole creation of a texture, load image and copy data, generate mipmaps
     */
    public final static String CREATE_TEXTURE = "CREATE_TEXTURE";
    /**
     * Load image, plus copy to {@link Image}
     */
    public final static String CREATE_IMAGE = "CREATE_IMAGE";
    /**
     * Load and decode of image to native format.
     */
    public final static String LOAD_IMAGE = "LOAD_IMAGE";
    /**
     * Copy native image to nucleus {@link Image}
     */
    public final static String COPY_IMAGE = "COPY_IMAGE";
    public final static String GENERATE_MIPMAPS = "GENERATE_MIPMAPS";

    public final static int DEFAULT_MIN_FPS = 30;
    private static FrameSampler frameSampler = new FrameSampler();

    /**
     * Start time of sampler
     */
    private final long samplerStart = System.currentTimeMillis();
    private long previousTime;
    private long currentTime;
    private int minFPS = DEFAULT_MIN_FPS;
    private float delta = (float) 1 / DEFAULT_MIN_FPS;
    private float maxDelta;

    private float totalDelta;
    private int frames;
    private long vertices;
    private long indices;
    private int drawCalls;
    private long sampleStart;


    /**
     * Returns the sampler instance
     * 
     * @return
     */
    public static FrameSampler getInstance() {
        return frameSampler;
    }

    /**
     * Updates to the current time, returning the delta time in seconds from previous frame this value will be corrected
     * by the min fps value.
     * 
     * @return Delta time in seconds from previous frame, this value will be checked for minimum fps. If min fps is 10
     * then this value will not be greater than 1/10 second.
     * The max delta value will be returned the first time this method is called (ie the first frame)
     */
    public float update() {
        previousTime = currentTime;
        currentTime = System.currentTimeMillis();
        delta = (float) (currentTime - previousTime) / 1000;
        frames++;
        totalDelta += delta;
        if (delta > (1f / minFPS)) {
            delta = 1f / minFPS;
        }
        return delta;
    }

    /**
     * Sets the min fps value
     * 
     * @param minFps
     */
    public void setMinFps(int minFps) {
        minFPS = minFps;

    }

    /**
     * Adds the number of vertices sent to drawArrays
     * 
     * @param vertices Number of vertices
     */
    public void addDrawArrays(int vertices) {
        this.vertices += vertices;
        drawCalls++;
    }

    public void addDrawElements(int vertices, int indices) {
        this.vertices += vertices;
        this.indices += indices;
        drawCalls++;
    }

    /**
     * Returns the current delta value, time in seconds from previous frame.
     * If a call to {@link #setMinFPS(int)} has been made then the delta value is limited according to this.
     * Will be 1 / DEFAULT_MIN_FPS before the first frame has finished.
     * 
     * @return The delta value for previous -> current frame, will be limited if {@link #setMinFPS(int)} has been
     * called.
     */
    public float getDelta() {
        if (maxDelta > 0) {
            if (delta > maxDelta) {
                return maxDelta;
            }
        }
        return delta;
    }

    /**
     * Returns the average fps, resetting the average values and setting sample start to the current time.
     * 
     * @return Average FPS info - same as calling toString() then resetting the values with clear()
     */
    public String sampleFPS() {
        String info = toString();
        clear();
        sampleStart = currentTime;
        return info;
    }

    /**
     * Returns the seconds from last call to sampleFPS()
     * 
     * @return
     */
    public float getSampleDuration() {
        if (sampleStart == 0) {
            sampleStart = System.currentTimeMillis();
            return 0;
        }
        return (int) (currentTime - sampleStart) / 1000;
    }

    /**
     * Clears all sample values
     */
    public void clear() {
        totalDelta = 0;
        frames = 0;
        indices = 0;
        vertices = 0;
        drawCalls = 0;
    }

    /**
     * Sets the min fps value, a value of 20 means that the delta-time, as returned by getDelta(), will never go above
     * 50 milliseconds (1/20 s).
     * Use this to limit the lowest fps for animations/logic - note that slowdown will occur of the client platform
     * cannot provide a fps that is higher.
     * 
     * @param fps Min fps, the value of getDelta() will be larger than (1/fps)
     */
    public void setMinFPS(int fps) {
        maxDelta = (float) 1 / fps;
    }

    @Override
    public String toString() {
        int fps = (int) (frames / totalDelta);
        return "Average FPS: " + fps + ", " + vertices / frames + " vertices, " + indices / frames
                + " indices, " + drawCalls / frames + " drawcall - per frame";
    }

    /**
     * Returns number of millis since the sampler was started, until now
     * 
     * @return
     */
    public long getMillisFromStart() {
        return getMillisFromStart(System.currentTimeMillis());
    }

    /**
     * Returns the number of millis since the sampler was started and now
     * 
     * @param now
     * @return Number of millis between start of sampler and now
     */
    public long getMillisFromStart(long now) {
        return now - samplerStart;
    }

    /**
     * Outputs the time between start of sampler and now, use this to measure startup time and similar
     * 
     * @param tag Identifier for sample
     */
    public void logTag(String tag) {
        logTag(tag, samplerStart, System.currentTimeMillis());
    }

    /**
     * Outputs the time between start of sampler and endtime
     * 
     * @param tag Identifier for sample
     * @param endTime Time when sample ended
     */
    public void logTag(String tag, long endTime) {
        logTag(tag, samplerStart, endTime);
    }

    /**
     * Outputs the time between startTime and endtime
     * 
     * @param tag Identifier for sample
     * @param startTime Time when sample started
     * @param endTime Time when sample ended
     */
    public void logTag(String tag, long startTime, long endTime) {
        SimpleLogger.d(getClass(), "Sample " + tag + " : " + (endTime - startTime) + " millis.");
    }

}
