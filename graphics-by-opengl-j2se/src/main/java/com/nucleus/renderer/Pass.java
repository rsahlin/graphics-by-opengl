package com.nucleus.renderer;

import com.nucleus.common.Flags;

/**
 * Definition of known passes
 *
 */
public enum Pass implements Flags {
    MAIN(1),
    SHADOW(2),
    LOGIC(4),
    /**
     * Used in the scenegraph before renderpass is set, this is the default state.
     */
    UNDEFINED(8),
    ALL(-1);
    
    /**
     * Bitwise value for enum
     */
    public final int flag;
    
    private Pass(int flag) {
        this.flag = flag;
    }

    @Override
    public int getFlags() {
        return flag;
    }
    
}
