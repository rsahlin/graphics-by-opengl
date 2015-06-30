package com.nucleus.io;

/**
 * Used to import data from external sources into a class
 * Use this with classes that contain the setup data, the goal is to move serialization from the implementation
 * class, for instance the geometry classes in Nucleus.
 * 
 * @author Richard Sahlin
 *
 */
public interface DataImporter {

    /**
     * Import data from String array where each String is one element of data, exactly what this means is implementation
     * specific
     * 
     * @param data String array with the data to import, implementing classes shall make the mapping.
     * @param offset The starting offset in the data offset for the current data to import
     * @return The number of elements consumed, this is used when subclasses call super.importData() to know size of
     * data
     * in superclass.
     */
    public int importData(String[] data, int offset);

}
