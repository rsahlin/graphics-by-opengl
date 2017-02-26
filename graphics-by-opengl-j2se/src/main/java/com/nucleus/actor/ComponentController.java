package com.nucleus.actor;

/**
 * Controls the state of components (logic), this is used to start, pause and reset the component.
 * 
 * @author Richard Sahlin
 *
 */
public interface ComponentController {

    /**
     * States for an action controller
     * 
     * @author Richard Sahlin
     *
     */
    public enum ComponentState {
        /**
         * Set when the the component is created
         */
        CREATED(),
        /**
         * Set then the component is initialized
         */
        INITIALIZED(),
        /**
         * Set when the component has been stopped after playing/paused, or when the object has been reset
         */
        STOPPED(),
        PLAY(),
        PAUSE();
    }

    /**
     * Sets the controller in the {@value ComponentState#PLAY} state, component processing will be done using delta time
     */
    public void play();

    /**
     * Sets the controller in the {@value ComponentState#PAUSE} state, component processing will NOT be done
     * Call {@link #play()} to resume
     */
    public void pause();

    /**
     * Sets the controller in the stopped state, component processing will not be done.
     * This is for components that shall have specific stop behavior.
     * The state shall be set to {@value ComponentState#STOPPED} Call {@link #play()} to start the component again.
     * 
     */
    public void stop();

    /**
     * Resets the controller to initial values, for instance setting position or animations.
     * The state shall be set to {@value ComponentState#STOPPED} Call {@link #play()} to start the component
     */
    public void reset();

    /**
     * Initializes the controller, the state shall be {@value ComponentState#INITIALIZED} after this method has been called
     */
    public void init();

    /**
     * Returns the state of the controller
     * 
     * @return
     */
    public ComponentState getControllerState();

}
