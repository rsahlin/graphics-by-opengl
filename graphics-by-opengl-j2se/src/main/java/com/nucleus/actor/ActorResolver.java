package com.nucleus.actor;

/**
 * Deprecated, do not use this interface as it forces each sprite to be processed separately and this will not
 * easily be accellerated.
 * Use System - component instead.
 * Interface used to find a Sprite actor class from String/Binary id
 * 
 * @author Richard Sahlin
 *
 */
@Deprecated
public interface ActorResolver {

    /**
     * Returns the sprite actor class for the specified id, this is normally done when loading scene or when
     * creating actor from loaded data.
     * 
     * @param id The id of the actor/sprite object
     * @return The sprite actor object or null if not found
     */
    ActorItem getActor(String id);

}