package com.nucleus.component;

/**
 * An error in a {@linkplain Component}
 * 
 * @author Richard Sahlin
 *
 */
public class ComponentException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -6723284286849031859L;

    public ComponentException(String message) {
        super(message);
    }

    public ComponentException(Throwable cause) {
        super(cause);
    }


}
