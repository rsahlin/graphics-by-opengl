package com.nucleus.gltf.loader;

import com.nucleus.J2SELogger;
import com.nucleus.SimpleLogger;

/**
 * Base testcase
 *
 */
public class BaseTestCase {

    public BaseTestCase() {
        SimpleLogger.setLogger(new J2SELogger());
    }

}
