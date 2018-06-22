package com.nucleus.mmi.core;

import com.nucleus.mmi.KeyEvent;

/**
 * Low level key intput listener, this is used to pass platform specific key based input events, such as keyboard or
 * gamepad.
 * 
 */
public interface KeyListener {

    /**
     * Callback for keyboard input event - joysticks and keypads are normally handled by this interface.
     * 
     * @param event
     */
    public void onKeyEvent(KeyEvent event);

}
