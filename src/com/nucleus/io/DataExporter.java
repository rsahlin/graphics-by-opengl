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

    /**
     * Exports the data in the class as one String, this shall be in a format that can be imported using
     * {@link DataImporter} This shall be the same as exporting to array then using default delimiter to combine the
     * strings.
     * 
     * @return String containing the data
     */
    public String exportDataAsString();

    /**
     * Exports the data in this class as String array, how the arrays are combined is up to the implementation.
     * 
     * @return Array containing the data.
     */
    public String[] exportDataAsStringArray();

}
