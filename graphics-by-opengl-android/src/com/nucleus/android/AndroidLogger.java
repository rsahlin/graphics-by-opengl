package com.nucleus.android;

import com.nucleus.SimpleLogger.Logger;

import android.util.Log;

public class AndroidLogger implements Logger {
    @Override
    public void d(Class clazz, String message) {
        Log.d(clazz.getCanonicalName(), message);
    }
}
