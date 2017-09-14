package com.nucleus.scene;

import com.nucleus.common.Type;

/**
 * The node types
 * 
 * @author Richard Sahlin
 *
 */
public enum NodeType implements Type<Node> {

    node(Node.class),
    layernode(LayerNode.class),
    switchnode(SwitchNode.class),
    linedrawernode(LineDrawerNode.class);

    private final Class<?> theClass;

    private NodeType(Class<?> theClass) {
        this.theClass = theClass;
    }

    /**
     * Returns the class to instantiate for the different types
     * 
     * @return
     */
    @Override
    public Class<?> getTypeClass() {
        return theClass;
    }

    @Override
    public String getName() {
        return name();
    }
    
}
