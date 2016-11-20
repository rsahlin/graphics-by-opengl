package com.nucleus.scene;

/**
 * An error in a {@linkplain Node}
 * 
 * @author Richard Sahlin
 *
 */
public class NodeException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 7279916623645807204L;

    public NodeException(String message) {
        super(message);
    }

    public NodeException(Throwable cause) {
        super(cause);
    }
}
