package com.nucleus.logic;

/**
 * Interface used to find a Sprite actor class from String/Binary id
 * 
 * @author Richard Sahlin
 *
 */
public interface ActorResolver {

    /**
     * Returns the sprite logic class for the specified id, this is normally done when loading scene or when
     * creating logic from loaded data.
     * 
     * @param id The id of the sprite object
     * @return The sprite logic object or null if not found
     */
    ActorItem getLogic(String id);

}