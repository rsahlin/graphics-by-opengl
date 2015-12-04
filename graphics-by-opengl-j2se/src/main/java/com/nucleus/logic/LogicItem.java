package com.nucleus.logic;

/**
 * Interface for one logic item, ie one logic element.
 * Logic items are usually put in a logic container {@link LogicContainer} and contained within a {@link LogicNode}
 * 
 * @author Richard Sahlin
 *
 */
public interface LogicItem {

    /**
     * Resets the logic, this shall prepare the object to do processing in the {@link #process(LogicContainer, float)}
     * method.
     * This method may be called any time after {@link #init(LogicContainer)} has been called.
     * 
     * @param logic
     */
    public void reset(LogicContainer logic);

    /**
     * Initializes the logic, preparing any data that may be fetched or processed before processing can begin.
     * This method will be called once before processing starts.
     * 
     * @param logic
     */
    public void init(LogicContainer logic);

    /**
     * Do the processing of the sprite, this shall be called at intervals to do the logic processing.
     * The sprite data containing data is decoupled from the behavior
     * 
     * @param sprite The sprite to perform behavior for.
     * @param deltaTime Time in millis since last call.
     */
    public void process(LogicContainer logic, float deltaTime);

    /**
     * Returns the name of the logic, ie the name of the implementing logic class.
     * This name is the same for all logic object of the same class, it is not instance name.
     * This shall be the same name that was used when the sprite logic was resolved.
     * 
     * @return The name of the implementing logic class
     */
    public String getLogicId();
}