package com.nucleus.io;

/**
 * Base class for holding setup data for a data class, this is to remove serialization from the implementing classes.
 * For instance the geometry classes in Nucleus.
 * Use factory methods that take setup classes with configuration (width, height, visuals etc) and return the class
 * holding the actual data and buffers
 * 
 * @author Richard Sahlin
 *
 */
public abstract class DataSetup extends BaseReference implements DataImporter, DataExporter {

    public interface Indexer {
        public int getIndex();
    }

    /**
     * Internal utility method to fetch String from data array.
     * 
     * @param data Array containing String data
     * @param offset Offset into array where data begins
     * @param index Index to add to offset for String to return.
     * @return The string at offset + index
     */
    protected String getString(String[] data, int offset, Indexer index) {
        return data[offset + index.getIndex()];
    }

    /**
     * Internal utility method to fetch int from data String array.
     * 
     * @param data Array containing String data
     * @param offset Offset into array where data begins
     * @param index Index to add to offset for int to return.
     * @return parseInt() of value at offset + index
     */
    protected int getInt(String[] data, int offset, Indexer index) {
        return Integer.parseInt(data[offset + index.getIndex()]);
    }

    /**
     * Internal utility method to fetch float from data String array.
     * 
     * @param data Array containing String data
     * @param offset Offset into array where data begins
     * @param index Index to add to offset for int to return.
     * @return parseFloat() of value at offset + index
     */
    protected float getFloat(String[] data, int offset, Indexer index) {
        return Float.parseFloat(data[offset + index.getIndex()]);
    }

}
