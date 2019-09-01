package com.nucleus.common;

public class Platform {

    public static final String OS_NAME = "os.name";
    public static final String JAVA_VENDOR = "java.vendor";

    public enum OS {
        windows(),
        linux(),
        macos(),
        android(),
        unknown();

        private OS() {
        }

        public static OS getOS(String osName, String vendor) {
            if (osName == null) {
                return null;
            }
            if (osName.contains("windows")) {
                return windows;
            }
            if (osName.contains("mac")) {
                return macos;
            }
            if (osName.contains("linux")) {
                if (vendor.contains("android")) {
                    return android;
                }
                return linux;
            }
            return unknown;
        }

    }

    private static Platform instance;
    private OS os;

    private Platform() {
        String osName = System.getProperty(OS_NAME);
        String vendor = System.getProperty(JAVA_VENDOR, "");
        os = OS.getOS(osName, vendor);
    }

    /**
     * Returns the singleton platform instance
     * 
     * @return
     */
    public static Platform getInstance() {
        if (instance == null) {
            instance = new Platform();
        }
        return instance;
    }

    public OS getOS() {
        return os;
    }

}
