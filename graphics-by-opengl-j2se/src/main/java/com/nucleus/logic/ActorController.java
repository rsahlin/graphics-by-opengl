package com.nucleus.logic;

/**
 * Controls the state of actors (logic), this is used to start, pause and reset the logic behavior.
 * An actor controller may control one or more logic items {@link ActorItem} The initial state for actors shall be
 * {@value State#STOPPED}
 * 
 * @author Richard Sahlin
 *
 */
public interface ActorController {

    /**
     * States for an action controller
     * 
     * @author Richard Sahlin
     *
     */
    public enum State {
        /**
         * Set when the the object is created
         */
        CREATED(),
        /**
         * Set then the object is initialized
         */
        INITIALIZED(),
        /**
         * Set when the object has been stopped after playing/paused, or when the object has been reset
         */
        STOPPED(),
        PLAY(),
        PAUSE();
    }

    /**
     * Sets the controller in the {@value State#PLAY} state, logic processing will be done using delta time
     */
    public void play();

    /**
     * Sets the controller in the {@value State#PAUSE} state, logic processing will NOT be done
     * Call {@link #play()} to resume
     */
    public void pause();

    /**
     * Sets the controller in the stopped state, logic processing will not be done.
     * This is for actors that shall have specific stop behavior.
     * The state shall be set to {@value State#STOPPED} Call {@link #play()} to start the actor again.
     * 
     */
    public void stop();

    /**
     * Resets the controller to initial values, for instance setting position or animations.
     * The state shall be set to {@value State#STOPPED} Call {@link #play()} to start the actor
     */
    public void reset();

    /**
     * Initializes the controller, the state shall be {@value State#INITIALIZED} after this method has been called
     */
    public void init();

    /**
     * Returns the state of the controller
     * 
     * @return
     */
    public State getControllerState();

}
