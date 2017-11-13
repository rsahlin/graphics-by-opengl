package com.nucleus;

/**
 * Very simple logger that prints a message.
 * 
 * @author Richard Sahlin
 *
 */
public class SimpleLogger {

    private static Logger logger;

    public interface Logger {
        /**
         * Logs a message for the specified class, the class name will be used as tag.
         * 
         * @param clazz
         * @param message
         */
        public void d(Class clazz, String message);

        /**
         * Logs a message for the specified tag.
         * 
         * @param tag
         * @param message
         */
        public void d(String tag, String message);

    }

    /**
     * Sets the logger implementation to use, must be called before any message is logged.
     * 
     * @param logger
     */
    public static void setLogger(Logger logger) {
        SimpleLogger.logger = logger;
    }

    /**
     * Logs a message for the specified class.
     * 
     * @param clazz
     * @param message
     * @throws NullPointerException If logger has not been set by calling {@link #setLogger(Logger)}
     */
    public static void d(Class clazz, String message) {
        logger.d(clazz, message);
    }

    /**
     * Logs a message for the tag
     * 
     * @param tag
     * @param message
     */
    public static void d(String tag, String message) {
        logger.d(tag, message);
    }

}
