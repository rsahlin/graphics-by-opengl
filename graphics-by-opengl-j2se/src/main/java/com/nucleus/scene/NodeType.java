package com.nucleus.scene;

import com.nucleus.common.Key;

/**
 * The node types
 * 
 * @author Richard Sahlin
 *
 */
public enum NodeType implements Key {

    node(Node.class),
    layernode(LayerNode.class),
    switchnode(SwitchNode.class);

    private final Class<?> theClass;

    private NodeType(Class<?> theClass) {
        this.theClass = theClass;
    }

    /**
     * Returns the class to instantiate for the different types
     * 
     * @return
     */
    public Class<?> getTypeClass() {
        return theClass;
    }

    @Override
    public String getKey() {
        return name();
    }
    
}
