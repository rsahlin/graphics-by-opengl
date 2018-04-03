package com.nucleus.mmi;

import com.nucleus.scene.Node;

/**
 * Listener for (high level) input events on a (visible) object
 * This is mostly intended for ui elements
 *
 */
public interface ObjectInputListener {

    public class EventConfiguration {

        public EventConfiguration() {

        }

        public EventConfiguration(float clickThreshold, float clickDeltaThreshold) {
            this.clickThreshold = clickThreshold;
            this.clickDeltaThreshold = clickDeltaThreshold;
        }

        /**
         * event press and release must be within this threshold (in seconds) for a click to be registered.
         */
        private float clickThreshold = 0.3f;

        /**
         * event press and release may not move more than this threshold (in viewport units) for a click to be
         * registered.
         */
        private float clickDeltaThreshold = 0.01f;

        /**
         * Returns the time, in seconds, that event release must be registered after event press for a click to be
         * registered.
         * 
         * @return Threshold time in seconds for a click to be registered.
         */
        public float getClickThreshold() {
            return clickThreshold;
        }

        /**
         * Returns the delta movement, in viewport units, for a click to be registered as click.
         * 
         * @return
         */
        public float getClickDeltaThreshold() {
            return clickDeltaThreshold;
        }

    }

    /**
     * Called when the object has a pointer action performed on it, check PointerData for action.
     * 
     * @param node The object that the input event is recorded on
     * @param event The pointer data for the event
     * @return True if the object consumes the pointer event
     */
    public boolean onInputEvent(Node node, PointerData event);

    /**
     * Called when a pointer down has been recorded and pointer has been dragged.
     * This may be interpreted as a drag, rectangle select or a swipe by the client.
     * 
     * @param node The object that the input event is recorded on
     * @param drag The pointer events making up the drag motion
     * @return True if the object consumes the action
     */
    public boolean onDrag(Node node, PointerMotionData drag);

    /**
     * Called when a click (down followed by up within time and distance limit)
     * 
     * @param node The object that the input event is recorded on
     * @param event pointer data for touch up event that triggered the onClick action.
     * @return True if object consumes the action.
     */
    public boolean onClick(Node node, PointerData event);

    /**
     * Return the inputlistener configuration
     * 
     * @return
     */
    public EventConfiguration getConfiguration();

}
