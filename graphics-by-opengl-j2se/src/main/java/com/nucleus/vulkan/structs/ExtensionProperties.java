package com.nucleus.vulkan.structs;

public class ExtensionProperties {
    public ExtensionProperties(String name, int specVersion) {
        this.name = name;
        this.specVersion = specVersion;
    }

    String name;
    int specVersion;

    public String getName() {
        return name;
    }

}