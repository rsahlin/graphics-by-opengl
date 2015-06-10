package com.nucleus.io;

/**
 * Used to export data to external sources from a class
 * Use this with classes that contain the setup data, the goal is to move serialization from the implementation
 * class, for instance the geometry classes in Nucleus.
 * 
 * @author Richard Sahlin
 */
public interface DataExporter {

    /**
     * The default delimiter used when exporting data. Eg 1,2,3,4,5
     */
    public final static String DEFAULT_DELIMITER = ",";

    public String exportDataAsString();

}
