package com.nucleus;

/**
 * Very simple logger that prints a message.
 * @author Richard Sahlin
 *
 */
public class SimpleLogger {

    /**
     * Logs a message for the specified class.
     * 
     * @param clazz
     * @param message
     */
    public static void d(Class clazz, String message) {
        System.out.println(clazz.getCanonicalName() + " " + message);
    }
}
