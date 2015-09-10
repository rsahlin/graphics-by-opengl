package com.nucleus.io;

import com.nucleus.common.StringUtils;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.types.DataType;

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

    public interface DataIndexer {
        public int getIndex();

        public DataType getType();
    }

    /**
     * Default constructor
     */
    public DataSetup() {
        super();
    }

    /**
     * Constructs a new DataSetup with base reference id
     * 
     * @param id
     */
    public DataSetup(String id) {
        super(id);
    }

    /**
     * Internal utility method to fetch String from data array.
     * 
     * @param data Array containing String data
     * @param offset Offset into array where data begins
     * @param index Index to add to offset for String to return.
     * @return The string at offset + index
     */
    protected String getString(String[] data, int offset, DataIndexer index) {
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
    protected int getInt(String[] data, int offset, DataIndexer index) {
        return Integer.parseInt(data[offset + index.getIndex()]);
    }

    /**
     * Utility method to set the int value as String at the index
     * 
     * @param value
     * @param data
     * @param index
     */
    protected void setInt(int value, String[] data, DataIndexer index) {
        data[index.getIndex()] = Integer.toString(value);
    }

    /**
     * Internal utility method to fetch float from data String array.
     * 
     * @param data Array containing String data
     * @param offset Offset into array where data begins
     * @param index Index to add to offset for int to return.
     * @return parseFloat() of value at offset + index
     */
    protected float getFloat(String[] data, int offset, DataIndexer index) {
        return Float.parseFloat(data[offset + index.getIndex()]);
    }

    /**
     * Internal utility method, returns an int as a String
     * 
     * @param number
     * @return Integer.toString(number)
     */
    protected String toString(int number) {
        return Integer.toString(number);
    }

    /**
     * Internal utility method, returns a float as a String
     * 
     * @param number
     * @return Float.toString(number);
     */
    protected String toString(float number) {
        return Float.toString(number);
    }

    /**
     * Sets the value as a String at the index corresponding to the dataindexer, use this when exporting data.
     * 
     * @param data Destination array
     * @param type The index where value is stored
     * @param value Store this value in data as String.
     */
    protected void setData(String[] data, DataIndexer type, int value) {
        data[type.getIndex()] = Integer.toString(value);
    }

    /**
     * Sets the value as String at the index from type, use this when exporting data.
     * 
     * @param data
     * @param type
     * @param value
     */
    protected void setData(String[] data, DataIndexer type, int[] value) {
        data[type.getIndex()] = StringUtils.getString(value);

    }

    /**
     * Sets the value as String at the index from type, use this when exporting data.
     * 
     * @param data
     * @param type
     * @param value
     */
    protected void setData(String[] data, DataIndexer type, float[] value) {
        data[type.getIndex()] = StringUtils.getString(value);

    }

    /**
     * Sets the value as a String at the index corresponding to the dataindexer, use this when exporting data.
     * 
     * @param data Destination array
     * @param type The index where value is stored
     * @param value Store this value in data as String.
     */
    protected void setData(String[] data, DataIndexer type, String value) {
        data[type.getIndex()] = value;
    }

    /**
     * Sets the value as a String at the index corresponding to the dataindexer, use this when exporting data.
     * 
     * @param data Destination array
     * @param type The index where value is stored
     * @param value Store this value in data as String.
     */
    protected void setData(String[] data, DataIndexer type, float value) {
        data[type.getIndex()] = Float.toString(value);
    }

    /**
     * Sets the value as a RESOLUTION enum at the index of the type, use this when exporting data.
     * 
     * @param data
     * @param type
     * @param value
     */
    protected void setData(String[] data, DataIndexer type, RESOLUTION value) {
        data[type.getIndex()] = value.toString();
    }

}
