package com.nucleus.actor;

/**
 * Interface for one actor item, ie one logic element.
 * Actor items are usually put in a actor container {@link ActorContainer} and contained within a {@link ActorNode}
 * 
 * @author Richard Sahlin
 *
 */
public interface ActorItem {

    /**
     * Resets the logic, this shall prepare the object to do processing in the {@link #process(ActorContainer, float)}
     * method.
     * This method may be called any time after {@link #init(ActorContainer)} has been called.
     * 
     * @param logic
     */
    public void reset(ActorContainer logic);

    /**
     * Initializes the logic, preparing any data that may be fetched or processed before processing can begin.
     * This method will be called once before processing starts.
     * 
     * @param logic
     */
    public void init(ActorContainer logic);

    /**
     * Do the processing of the sprite, this shall be called at intervals to do the logic processing.
     * The sprite data containing data is decoupled from the behavior
     * 
     * @param sprite The sprite to perform behavior for.
     * @param deltaTime Time in millis since last call.
     */
    public void process(ActorContainer logic, float deltaTime);

    /**
     * Returns the name of the logic, ie the name of the implementing logic class.
     * This name is the same for all logic object of the same class, it is not instance name.
     * This shall be the same name that was used when the sprite logic was resolved.
     * 
     * @return The name of the implementing logic class
     */
    public String getLogicId();
}