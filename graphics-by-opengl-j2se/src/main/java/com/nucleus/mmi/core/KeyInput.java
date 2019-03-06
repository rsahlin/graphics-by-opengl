package com.nucleus.mmi.core;

import com.nucleus.mmi.Key;

/**
 * Low level key input listener, this is used to pass platform specific key based input events, such as keyboard or
 * gamepad.
 * 
 */
public interface KeyInput {

    /**
     * Callback for keyboard input event - joysticks and keypads are normally handled by this interface.
     * 
     * @param event
     */
    public void onKeyEvent(Key event);

}
