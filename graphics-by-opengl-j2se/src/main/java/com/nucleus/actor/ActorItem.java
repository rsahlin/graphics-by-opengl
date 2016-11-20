package com.nucleus.actor;

/**
 * Interface for one actor item, ie one actor element.
 * Actor items are usually put in a actor container {@link ActorContainer} and contained within a {@link ComponentNode}
 * 
 * @author Richard Sahlin
 *
 */
public interface ActorItem {

    /**
     * Resets the actor, this shall prepare the object to do processing in the {@link #process(ActorContainer, float)}
     * method.
     * This method may be called any time after {@link #init(ActorContainer)} has been called.
     * 
     * @param actor
     */
    public void reset(ActorContainer actor);

    /**
     * Initializes the actor, preparing any data that may be fetched or processed before processing can begin.
     * This method will be called once before processing starts.
     * 
     * @param actor
     */
    public void init(ActorContainer actor);

    /**
     * Do the processing of the sprite, this shall be called at intervals to do the actor processing.
     * The sprite data containing data is decoupled from the behavior
     * 
     * @param sprite The sprite to perform behavior for.
     * @param deltaTime Time in millis since last call.
     */
    public void process(ActorContainer actor, float deltaTime);

    /**
     * Returns the name of the actor, ie the name of the implementing actor class.
     * This name is the same for all actor object of the same class, it is not instance name.
     * This shall be the same name that was used when the sprite actor was resolved.
     * 
     * @return The name of the implementing actor class
     */
    public String getActorId();
}