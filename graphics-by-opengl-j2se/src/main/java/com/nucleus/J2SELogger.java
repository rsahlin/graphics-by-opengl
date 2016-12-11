package com.nucleus;

import com.nucleus.SimpleLogger.Logger;

public class J2SELogger implements Logger {
    @Override
    public void d(Class clazz, String message) {
        System.out.println(clazz.getCanonicalName() + " " + message);
    }
}
