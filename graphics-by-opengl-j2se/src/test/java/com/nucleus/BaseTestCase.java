package com.nucleus;

import org.junit.BeforeClass;

/**
 * Base testcase
 *
 */
public class BaseTestCase {

    @BeforeClass
    public static void beforeClass() {
        SimpleLogger.setLogger(new J2SELogger());
    }

}
