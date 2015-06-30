package com.nucleus.common;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class StringUtils {

    protected final static String DEFAULT_DELIMITER = ",";

    /**
     * Returns the string as a String[], delimetered by ','
     * 
     * @param str The string to return as array.
     * @return Array of Strings split by default delimiter, or null if str is null.
     */
    public static String[] getStringArray(String str) {
        if (str == null) {
            return null;
        }
        ArrayList<String> resultList = getArrayList(str, DEFAULT_DELIMITER);
        String[] result = new String[resultList.size()];
        int index = 0;
        for (String s : resultList) {
            result[index++] = s;
        }
        return result;
    }

    /**
     * Returns the String array as a String delimietred by ','
     * 
     * @param strArray
     * @return
     */
    public static String getString(String[] strArray) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < strArray.length; i++) {
            if (i > 0) {
                result.append(DEFAULT_DELIMITER);
            }
            result.append(strArray[i]);
        }
        return result.toString();
    }

    /**
     * Converts the int array to a String with values delimetered by ','
     * 
     * @param intArray
     * @return The int array as a String with values delimitered by ','
     */
    public static String getString(int[] intArray) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < intArray.length; i++) {
            if (i > 0) {
                result.append(DEFAULT_DELIMITER);
            }
            result.append(Integer.toString(intArray[i]));
        }
        return result.toString();
    }

    public static ArrayList<String> getArrayList(String str, String delimiter) {
        ArrayList<String> resultList = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreTokens()) {
            resultList.add(st.nextToken());
        }
        return resultList;
    }

    /**
     * Converts the String to an array of float, using ',' as delimiter for array items.
     * 
     * @param str The string containing floats to convert to float array.
     * @return Array of float or null if str is null.
     */
    public static float[] getFloatArray(String str) {
        if (str == null) {
            return null;
        }
        ArrayList<String> resultList = getArrayList(str, DEFAULT_DELIMITER);
        float[] result = new float[resultList.size()];
        int index = 0;
        for (String s : resultList) {
            result[index++] = Float.parseFloat(s);
        }
        return result;
    }

    /**
     * Converts the String to an array of int, using ',' as delimiter for array items.
     * 
     * @param str The string containing integers to convert to int array.
     * @return Array of int or null if str is null.
     */
    public static int[] getIntArray(String str) {
        if (str == null) {
            return null;
        }
        ArrayList<String> resultList = getArrayList(str, DEFAULT_DELIMITER);
        int[] result = new int[resultList.size()];
        int index = 0;
        for (String s : resultList) {
            result[index++] = Integer.parseInt(s);
        }
        return result;
    }

    /**
     * Converts the String to an array of short, using ',' as delimiter for array items.
     * 
     * @param str The string containing shorts to convert to short array.
     * @return Array of short or null if str is null.
     */
    public static short[] getShortArray(String str) {
        if (str == null) {
            return null;
        }
        ArrayList<String> resultList = getArrayList(str, DEFAULT_DELIMITER);
        short[] result = new short[resultList.size()];
        int index = 0;
        for (String s : resultList) {
            result[index++] = Short.parseShort(s);
        }
        return result;
    }

    /**
     * Creates a new String array combining the array and the string, the string is put last.
     * 
     * @param array
     * @param str
     * @return The resulting array, combining array and str where str is put last.
     */
    public static String[] append(String[] array, String str) {
        String[] result = new String[array.length + 1];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        result[array.length] = str;
        return result;
    }

}
