package com.nucleus.ui;

/**
 * Base interface for UI button
 *
 */
public interface Button extends Element {

    public interface ButtonListener {
        public void onButtonPressed();
    }

}
