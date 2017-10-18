package com.nucleus.common;

/**
 * Interface for bitwise complement flags
 * This is intended for objects that need to implement flag type of behavior
 *
 */
public interface Flags {
    
    /**
     * Returns the bitwise flag values, what the values means is implementation specific
     * @return One or more values bitwise ored together
     */
    public int getFlags();
    
}
