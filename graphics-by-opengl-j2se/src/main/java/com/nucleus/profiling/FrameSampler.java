package com.nucleus.profiling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.nucleus.SimpleLogger;

/**
 * Utility class for keeping track of delta times, normally used to calculate the delta time from one frame to the next.
 * Singleton class that can be
 * 
 * @author Richard Sahlin
 *
 */
public class FrameSampler {

    public interface SampleInfo {
        /**
         * Returns the tag for the sample
         * 
         * @return
         */
        public String getTag();

        /**
         * Returns the sample log detail level, used to filter sample logs.
         * 
         * @return
         */
        public Level getDetail();
    }

    public static class Sample {

        public int nano;
        public int total;
        public int max;
        public int min;
        public int count;
        public long startTime;

        /**
         * Creates an empty sample
         */
        public Sample() {
            startTime = System.currentTimeMillis();
        }

        /**
         * Creates a sample with one value
         * 
         * @param millis
         */
        public Sample(int millis) {
            startTime = System.currentTimeMillis();
            total = millis;
            max = millis;
            min = millis;
            count = 1;
        }

        /**
         * Adds a nanosecond sample, does not update any other values such as count, or min/max
         * Use this to aggregate multiple calls with nano precision
         * 
         * @param nano
         */
        public void addNano(int nano) {
            this.nano += nano;
        }

        /**
         * Adds a millisecond sample, min/max will be updated.
         * 
         * @param millis
         */
        public void add(int millis) {
            total += millis;
            updateMinMax(millis);
            count++;
        }

        private void updateMinMax(int millis) {
            if (max < millis) {
                max = millis;
            }
            if (min > millis) {
                min = millis;
            }

        }

        /**
         * Returns the number of values added to the sample
         * 
         * @return
         */
        public int getCount() {
            return count;
        }

        /**
         * Returns the average value, including time added to {@link #addNano(int)}
         * 
         * @return
         */
        public int getAverage() {
            int total = this.total + (nano / 1000000);
            if (total > 0 && count > 0) {
                return total / count;
            } else {
                return 0;
            }
        }

        /**
         * Resets the sample to be used again.
         */
        public void reset() {
            total = 0;
            nano = 0;
            max = 0;
            min = Integer.MAX_VALUE;
            count = 0;
            startTime = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "Average: " + getAverage() + " Max: " + max + " Min: " + min + ", number of values " + count;
        }
    }

    public enum Samples implements SampleInfo {

        DISPLAY_SPLASH(Level.NORMAL),
        SET_ROOT_NODE(Level.NORMAL),
        LOAD_SCENE(Level.NORMAL),
        CREATE_SCENE(Level.NORMAL),
        CREATE_NODE(Level.NORMAL),
        LOAD_MAP(Level.NORMAL),
        CREATE_SHADER(Level.NORMAL),
        PROCESSCOMPONENT(Level.HIGH),
        RENDERNODES(Level.NORMAL),
        CREATE_TEXTURE(Level.NORMAL),
        CREATE_IMAGE(Level.NORMAL),
        LOAD_IMAGE(Level.NORMAL),
        COPY_IMAGE(Level.NORMAL),
        UPLOAD_TEXTURE(Level.NORMAL),
        EGLSWAPBUFFERS(Level.HIGH),
        EGLWAITNATIVE(Level.HIGH),
        POINTER_INPUT(Level.NORMAL);

        public final Level detail;

        private Samples(Level detail) {
            this.detail = detail;
        }

        @Override
        public String getTag() {
            return name();
        }

        @Override
        public Level getDetail() {
            return detail;
        }

    }

    public static int DEFAULT_MIN_FPS = 0;
    public static float DEFAULT_FRAMEDELTA = 0.16f;
    private static FrameSampler frameSampler = new FrameSampler();

    private final static int DEFAULT_LOG_DELAY = 5000;

    /**
     * Start time of sampler
     */
    private final long samplerStart = System.currentTimeMillis();
    private long previousTime;
    private long currentTime;
    private int minFPS = DEFAULT_MIN_FPS;
    private float delta = DEFAULT_FRAMEDELTA;
    private float maxDelta;
    /**
     * sampling is auto logged after this number of millis
     */
    private int logDelay = DEFAULT_LOG_DELAY;

    private float totalDelta;
    private int frames;
    private long vertices;
    private long indices;
    private int drawCalls;
    private long sampleStart;

    private Map<String, Sample> tagTimings = new HashMap<>();
    private Map<String, ArrayList<Long>> tagStartTimes = new HashMap<>();

    public enum Level {
        LOW(1),
        NORMAL(2),
        HIGH(3);

        public final int value;

        private Level(int value) {
            this.value = value;
        }
    }

    /**
     * Adjust to log different sample timings, read/write this in your code
     */
    public Level sampleDetail = Level.NORMAL;

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
     * First frame the value {@link #DEFAULT_FRAMEDELTA} will be returned
     */
    public float update() {
        previousTime = currentTime;
        currentTime = System.currentTimeMillis();
        if (previousTime == 0) {
            return DEFAULT_FRAMEDELTA;
        }
        delta = (float) (currentTime - previousTime) / 1000;
        frames++;
        totalDelta += delta;
        if (minFPS > 0) {
            if (delta > (1f / minFPS)) {
                delta = 1f / minFPS;
            }
        }
        return delta;
    }

    /**
     * Sets the min fps value, or disables by setting to 0 or lower.
     * 
     * @param minFps Minimum fps value, in number of frames per second. Set to < 1 to disable.
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
     * Will be {@link #DEFAULT_FRAMEDELTA} before the first frame has finished.
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
        if (fps > 0) {
            maxDelta = (float) 1 / fps;
        } else {
            maxDelta = 0;
        }
    }

    @Override
    public String toString() {
        int fps = (int) (frames / totalDelta);
        return "Average FPS: " + fps + "(" + frames + " frames), " + vertices / frames + " vertices, "
                + indices / frames
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
     * @param info Identifier for sample
     */
    public void logTag(SampleInfo info) {
        logTag(info, samplerStart, System.currentTimeMillis());
    }

    /**
     * Outputs the time between start of sampler and endtime
     * 
     * @param info Identifier for sample
     * @param endTime Time when sample ended
     */
    public void logTag(SampleInfo info, long endTime) {
        logTag(info, samplerStart, endTime);
    }

    /**
     * Outputs the time between startTime and endtime
     * 
     * @param info Identifier for sample
     * @param startTime Time when sample started
     * @param endTime Time when sample ended
     */
    public void logTag(SampleInfo info, long startTime, long endTime) {
        if (info.getDetail().value >= sampleDetail.value) {
            SimpleLogger.d(getClass(), "Sample " + info.getTag() + " : " + (endTime - startTime) + " millis.");
        }
    }

    /**
     * Outputs the time between startTime and endtime
     * 
     * @param info Identifier for sample
     * @param extra Extra identifier tat
     * @param startTime Time when sample started
     * @param endTime Time when sample ended
     */
    public void logTag(SampleInfo info, String extra, long startTime, long endTime) {
        if (info.getDetail().value >= sampleDetail.value) {
            SimpleLogger.d(getClass(), "Sample " + info.getTag() + extra + " : " + (endTime - startTime) + " millis.");
        }
    }

    /**
     * Adds the tag timing, outputs min/max/average at specified interval
     * 
     * @param tag
     * @param startTime
     * @param endTime The end time of interval
     * @param detail The sample log level, if current level is equal or higher then this sample is added. Otherwise it
     * is skipped.
     */
    public void addTag(String tag, long startTime, long endTime, Level detail) {
        if (sampleDetail.value >= detail.value) {
            Sample sample = tagTimings.get(tag);
            int millis = (int) (endTime - startTime);
            if (sample == null) {
                sample = new Sample(millis);
                tagTimings.put(tag, sample);
            } else {
                sample.add(millis);
                autoLog(tag, sample);
            }
        }
    }

    /**
     * Checks if the log period for sample has been reached, if so the sample is logged using tag then reset.
     * 
     * @param tag
     * @param sample
     */
    public void autoLog(String tag, Sample sample) {
        if (sample.startTime + logDelay < System.currentTimeMillis()) {
            logAverage(tag, sample);
            sample.reset();
        }
    }

    /**
     * Adds the tag timing, outputs min/max/average at specified interval
     * 
     * @param info
     * @param startTime
     * @param endTime The end time of interval
     */
    public void addTag(SampleInfo info, long startTime, long endTime) {
        addTag(info.getTag(), startTime, endTime, info.getDetail());
    }

    /**
     * Adds start time to a tag, must be finalized with a call to {@link #setEndTimes(String, long)}
     * 
     * @param tag
     * @param startTime
     * @param detail The sample log level, if current level is equal or higher then this sample is added. Otherwise it
     * is skipped.
     */
    public void addTag(String tag, long startTime, Level detail) {
        if (sampleDetail.value >= detail.value) {
            synchronized (tagStartTimes) {
                ArrayList<Long> start = tagStartTimes.get(tag);
                if (start == null) {
                    start = new ArrayList<>();
                    tagStartTimes.put(tag, start);
                }
                start.add(startTime);
            }
        }
    }

    /**
     * Adds start time to a tag, must be finalized with a call to {@link #setEndTimes(SampleInfo, long)}
     * 
     * @param info
     * @param startTime
     */
    public void addTag(SampleInfo info, long startTime) {
        addTag(info.getTag(), startTime, info.getDetail());
    }

    /**
     * Sets the end times that have {@value #UNDEFINED}
     * 
     * @param tag
     * @param endTime
     * @param detail The sample log level, if current level is equal or higher then this sample is finalized.
     */
    public void setEndTimes(String tag, long endTime, Level detail) {
        synchronized (tagStartTimes) {
            ArrayList<Long> start = tagStartTimes.get(tag);
            if (start != null) {
                for (long s : start) {
                    addTag(tag, s, endTime, detail);
                }
            }
            tagStartTimes.clear();
        }
    }

    /**
     * Sets the end times that have {@value #UNDEFINED}
     * 
     * @param info
     * @param endTime
     */
    public void setEndTimes(SampleInfo info, long endTime) {
        setEndTimes(info.getTag(), endTime, info.getDetail());
    }

    /**
     * Returns the sample for the specified tag, or null if not found.
     * This is a reference to the Sample used to track values - changes will be reflected in the object stored
     * with the tag.
     * 
     * @param tag
     * @return Reference to the Sample, or null if no Sample for the tag
     */
    public Sample getSample(String tag) {
        return tagTimings.get(tag);
    }

    /**
     * Sets a sample for the tag
     * 
     * @param tag
     * @param sample Sample to set or null to clear.
     */
    public void setSample(String tag, Sample sample) {
        tagTimings.put(tag, sample);
    }

    private void logAverage(String tag, Sample sample) {
        SimpleLogger.d(getClass(), "Sampler tag " + tag + " : " + sample.toString());
    }

    /**
     * Sets the millisecond delay between logs when calling one of the #addTag() methods
     * 
     * @param millis
     */
    public void setLogDelay(int millis) {
        this.logDelay = millis;
    }

    /**
     * Returns the log delay in millis, this is the frequency of log output.
     * 
     * @return
     */
    public int getLogDelay() {
        return logDelay;
    }

}
