package com.nucleus.ui;

/**
 * Base interface for UI button
 *
 */
public interface Button extends Element {

    /**
     * Button has been pressed
     * 
     * @return true if the event was consumed
     */
    public interface ButtonListener {
        public boolean onPressed(Button button);
    }

    /**
     * The button was pressed, update button on screen and dispatch {@link ButtonListener} if attached.
     */
    public void pressed();

}
