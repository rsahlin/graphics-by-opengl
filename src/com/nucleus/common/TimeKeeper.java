package com.nucleus.common;

/**
 * Utility class for keeping track of delta times, normally used to calculate the delta time from one frame to the next.
 * 
 * @author Richard Sahlin
 *
 */
public class TimeKeeper {

    public final static int DEFAULT_MIN_FPS = 30;

    private long previousTime;
    private long currentTime;
    private int minFPS = DEFAULT_MIN_FPS;
    private float delta;

    private float totalDelta;
    private int frames;
    private long sampleStart;

    public TimeKeeper(int minFPS) {
        this.minFPS = minFPS;
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
     * Returns the current delta value, time in seconds from previous frame.
     * 
     * @return
     */
    public float getDelta() {
        return delta;
    }

    /**
     * Returns the average fps, resetting the average values and setting sample start to the current time.
     * 
     * @return Average FPS for the current time period.
     */
    public int sampleFPS() {
        int fps = (int) (frames / totalDelta);
        totalDelta = 0;
        frames = 0;
        sampleStart = currentTime;
        return fps;
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

}
