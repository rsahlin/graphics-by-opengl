package com.nucleus.utils;

import org.junit.Assert;

import com.nucleus.common.StringUtils;
import com.nucleus.io.DataSetup.DataIndexer;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureParameter.Name;
import com.nucleus.types.DataType;

public class DataSerializeUtils {

    /**
     * Utility method to set data as string from the DataType, this can be used when testing the import/export
     * functions.
     * 
     * @param type
     * @param destination
     * @param offset
     */
    public static void setDataAsString(int value, DataType type, String[] destination, int offset) {

        destination[offset] = DataSerializeUtils.getString(value, type);
    }

    /**
     * Utillity method to return the datatype as a String, for int,float,short this will be value.
     * For other datatypes, such as RESOLUTION, TEXTURE_PARAMETER default value(s) will be returned.
     * 
     * @param index
     * @param type
     * @return The datatype as String
     */
    public static String getString(int index, DataType type) {
        switch (type) {
        case FLOAT:
            return Float.toString(index);
        case INT:
            return Integer.toString(index);
        case RESOLUTION:
            return RESOLUTION.HD.toString();
        case SHORT:
            return Short.toString((short) index);
        case STRING:
            return Integer.toString(index);
        case TEXTURE_PARAMETER:
            switch (index) {
            case TextureParameter.MIN_FILTER:
                return Name.LINEAR.toString();
            case TextureParameter.MAG_FILTER:
                return Name.NEAREST.toString();
            case TextureParameter.WRAP_S:
                return Name.CLAMP.toString();
            case TextureParameter.WRAP_T:
                return Name.REPEAT.toString();
            default:
                throw new IllegalArgumentException("Not implemented");
            }
        default:
            throw new IllegalArgumentException("Not implemented");

        }

    }

    /**
     * Creates default datatype import data for testcases. Use this method when importing data into a setup file.
     * 
     * @param types
     * @return Array with String array containing defult data values for the specified types, can be used to import data
     * into setup.
     */
    public static String[] createDefaultData(DataIndexer[] types) {
        String[] data = new String[types.length];
        createDefaultData(types, data, 0);
        return data;
    }

    /**
     * Creates the specified types, storing as String in data at offset.
     * 
     * @param types The types to create
     * @param data Result array
     * @param offset Offset into array where result is stored.
     */
    public static void createDefaultData(DataIndexer[] types, String[] data, int offset) {
        for (int i = 0; i < types.length; i++) {
            DataIndexer t = types[i];
            DataSerializeUtils.setDataAsString(t.getIndex(), t.getType(), data, offset + i);
        }

    }

    /**
     * Creates the default data for base (superclass) and extending class, use when setup class
     * extends another setupclass.
     * 
     * @param base The superclass, ie the first datatypes to import
     * @param types The datatypes to import
     * @return Array with string data for both base + types.
     */
    public static String[] createDefaultData(DataIndexer[] base, DataIndexer[] types) {
        String[] data = new String[base.length + types.length];
        createDefaultData(base, data, 0);
        createDefaultData(types, data, base.length);
        return data;
    }

    public static void assertDataAsString(int[] expected, String[] actual, DataIndexer type) {
        int[] intArray = StringUtils.getIntArray(actual[type.getIndex()]);
    }

    public static void assertDataAsString(String[] expected, String[] actual, DataIndexer type) {
        String[] strArray = StringUtils.getStringArray(actual[type.getIndex()]);
    }

    public static void assertDataAsString(int expected, String[] actual, DataIndexer type) {
        Assert.assertEquals(Integer.toString(expected), actual[type.getIndex()]);
    }

    public static void assertDataAsString(int expected, String[] actual, DataIndexer type, int offset) {
        Assert.assertEquals(Integer.toString(expected), actual[type.getIndex() + offset]);
    }

    public static void assertDataAsString(float expected, String[] actual, DataIndexer type) {
        Assert.assertEquals(Float.toString(expected), actual[type.getIndex()]);
    }

    public static void assertDataAsString(float expected, String[] actual, DataIndexer type, int offset) {
        Assert.assertEquals(Float.toString(expected), actual[type.getIndex() + offset]);
    }

    public static void assertDataAsString(RESOLUTION expected, String[] actual, DataIndexer type) {
        Assert.assertEquals(expected.toString(), actual[type.getIndex()]);
    }

    public static void assertDataAsString(String expected, String[] actual, DataIndexer type) {
        Assert.assertEquals(expected, actual[type.getIndex()]);
    }

    public static void assertDataAsString(String expected, String[] actual, DataIndexer type, int offset) {
        Assert.assertEquals(expected, actual[type.getIndex() + offset]);
    }

    /**
     * Asserts that the actual values is the same as the String at the specified index.
     * Use this for instance when checking imported data
     * 
     * @param expected Array with expected values
     * @param index The index to check
     * @param actual The actual value
     * @param offset Offset into expected array
     */
    public static void assertString(String[] expected, DataIndexer index, int actual, int offset) {
        Assert.assertEquals(expected[index.getIndex() + offset], Integer.toString(actual));
    }

    /**
     * Asserts that the actual values is the same as the String at the specified index.
     * Use this for instance when checking imported data
     * 
     * @param expected Array with expected values
     * @param index The index to check
     * @param actual The actual value
     * @param offset Offset into expected array
     */
    public static void assertString(String[] expected, DataIndexer index, float actual, int offset) {
        Assert.assertEquals(expected[index.getIndex() + offset], Float.toString(actual));
    }

    /**
     * Asserts that the actual values is the same as the String at the specified index.
     * Use this for instance when checking imported data
     * 
     * @param expected Array with expected values
     * @param index The index to check
     * @param actual The actual value
     * @param offset Offset into expected array
     */
    public static void assertString(String[] expected, DataIndexer index, String actual, int offset) {
        Assert.assertEquals(expected[index.getIndex() + offset], actual);
    }

}
