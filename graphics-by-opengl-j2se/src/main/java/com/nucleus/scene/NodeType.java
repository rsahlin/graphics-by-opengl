package com.nucleus.scene;

import com.nucleus.common.Key;

/**
 * The node types
 * 
 * @author Richard Sahlin
 *
 */
public enum NodeType implements Key {

    node(),
    viewnode(),
    switchnode();

    @Override
    public String getKey() {
        return name();
    }

}
