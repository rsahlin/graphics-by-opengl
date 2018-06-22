package com.nucleus.mmi;

/**
 * Holds data for a key input event, this could be from keyboard or gamepad/joystick
 * 
 */
public class KeyEvent {

    /**
     * The key events
     *
     */
    public enum Action {

        PRESSED(0),
        RELEASED(1);

        private int action;

        private Action(int action) {
            this.action = action;
        }
    }

    public enum KeyCode {
        /**
         * Digital button 1, reported as key down or up event.
         */
        BUTTON_1(0x01),
        /**
         * Digital button 2, reported as key down or up event.
         */
        BUTTON_2(0x02),
        /**
         * Digital button 3, reported as key down or up event.
         */
        BUTTON_3(0x04),
        /**
         * Digital button 4, reported as key down or up event.
         */
        BUTTON_4(0x08),
        /**
         * Digital button 5, reported as key down or up event.
         */
        BUTTON_5(0x010),
        /**
         * Digital button 6, reported as key down or up event.
         */
        BUTTON_6(0x020),
        /**
         * Digital button 7, reported as key down or up event.
         */
        BUTTON_7(0x040),

        /**
         * Dpad left button, reported as key down or up event.
         */
        DPAD_LEFT(0x0200),
        /**
         * Dpad left button, reported as key down or up event.
         */
        DPAD_RIGHT(0x0400),
        /**
         * Dpad left button, reported as key down or up event.
         */
        DPAD_UP(0x0800),
        /**
         * Dpad left button, reported as key down or up event.
         */
        DPAD_DOWN(0x01000),
        /**
         * Key for back/cancel, reported as key down or up event.
         */
        BACK_KEY(0x02000),
        /**
         * Keypress on keyboard.
         */
        KEYBOARD(0x04000);

        public final int value;

        private KeyCode(int value) {
            this.value = value;
        }

    }

    /**
     * Default pressed pressure
     */
    public static final float PRESSED_PRESSURE = 1f;
    /**
     * Default release pressure
     */
    public static final float RELEASED_PRESSURE = 0f;

    private Action action;
    private KeyCode keyCode;
    private float pressure;
    /**
     * If a key on the keyboard is pressed it is registered here.
     */
    private int keyValue;

    /**
     * Creates a new key event for a non keyboard input event, for instance gamepad.
     * The pressure is set to {@value #PRESSED_PRESSURE} if action is {@link Action#PRESSED} and
     * {@value #RELEASED_PRESSURE} if action is {@link Action#RELEASED}
     * Use this for non keyboard digital inputs, such as a digital gamepad
     * 
     * @param action
     * @param keyCode
     */
    public KeyEvent(Action action, KeyCode keyCode) {
        this.action = action;
        this.keyCode = keyCode;
        switch (action) {
            case PRESSED:
                this.pressure = PRESSED_PRESSURE;
                break;
            case RELEASED:
                this.pressure = RELEASED_PRESSURE;
                break;
            default:
                throw new IllegalArgumentException("Not implemented for action " + action);
        }
    }

    /**
     * Creates a new key event for a non keyboard input event, for instance gamepad.
     * Use this for non keyboard analog type of key inputs, such as an analog gamepad key.
     * 
     * @param action
     * @param keyCode
     * @param pressure The keycode pressure
     */
    public KeyEvent(Action action, KeyCode keyCode, float pressure) {
        this.action = action;
        this.keyCode = keyCode;
        this.pressure = pressure;
    }

    /**
     * Creates a new key event for a keyboard input event - this constructor shall be used when the originating press
     * comes from the keyboard.
     * 
     * @param action
     * @param keyValue The keyboard key value.
     */
    public KeyEvent(Action action, int keyValue) {
        this.action = action;
        this.keyCode = KeyCode.KEYBOARD;
        this.keyValue = keyValue;

    }

}
