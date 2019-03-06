package com.nucleus.ui;

/**
 * Base interface for UI button
 *
 */
public interface Button extends Element {

    public enum Action {
        NONE(),
        /**
         * When there is a down - but now up, action on the button.
         */
        PRESSED(),
        /**
         * When down + up is recorded and the clicked behavior is invoked
         */
        CLICKED();
    }

    /**
     * Button has been clicked
     * 
     * @return true if the event was consumed
     */
    public interface ButtonListener {
        public boolean onClicked(Button button);
    }

}
