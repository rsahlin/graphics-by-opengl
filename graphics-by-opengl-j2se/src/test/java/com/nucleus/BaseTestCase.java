package com.nucleus;

/**
 * Base testcase
 *
 */
public class BaseTestCase {

    public BaseTestCase() {
        SimpleLogger.setLogger(new J2SELogger());
    }

}
