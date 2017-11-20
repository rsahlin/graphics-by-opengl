package com.nucleus.common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.nucleus.SimpleLogger;

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
     * Returns the String array as a String delimitered by ','
     * Same as calling {@link #getString(String[], int, int)} with offset 0 and count = length
     * 
     * @param strArray
     * @return The resulting String
     */
    public static String getString(String[] strArray) {
        return getString(strArray, 0, strArray.length);
    }

    /**
     * Returns the String array, beginning with array offset, as one String delimitered by ','
     * 
     * @param strArray
     * @param offset The first array to include
     * @param count Number of arrays to copy. Must be <= strArray.length - offset
     * @return The resulting String
     */
    public static String getString(String[] strArray, int offset, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                result.append(DEFAULT_DELIMITER);
            }
            result.append(strArray[offset++]);
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
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < intArray.length; i++) {
            if (i > 0) {
                result.append(DEFAULT_DELIMITER);
            }
            result.append(Integer.toString(intArray[i]));
        }
        return result.toString();
    }

    /**
     * Converts the float array to a String with values delimetered by ','
     * 
     * @param floatArray
     * @return The float array as a String with values delimitered by ','
     */
    public static String getString(float[] floatArray) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < floatArray.length; i++) {
            if (i > 0) {
                result.append(DEFAULT_DELIMITER);
            }
            result.append(Float.toString(floatArray[i]));
        }
        return result.toString();
    }

    /**
     * Returns the list as an array, using delimeter
     * 
     * @param str
     * @param delimiter
     * @return
     */
    public static ArrayList<String> getArrayList(String str, String delimiter) {
        ArrayList<String> resultList = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreTokens()) {
            resultList.add(st.nextToken());
        }
        return resultList;
    }

    /**
     * Returns the sequence of Strings, using delimiter, as a List
     * 
     * @return
     */
    public static List<String> getList(String str, String delimiter) {
        List<String> result = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreTokens()) {
            String extension = st.nextToken();
            result.add(extension);
        }
        return result;
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

    /**
     * Creates a String using UTF-8 encoding from the byte array.
     * Use this method to make sure UTF-8 is used regardless of platform encoding.
     * 
     * @param data
     * @param start
     * @param length
     * @return
     */
    public static String createString(byte[] data, int start, int length) {
        return new String(data, start, length, StandardCharsets.UTF_8);
    }

    /**
     * Logs each String in the list as a log message.
     * 
     * @param tag
     * @param list
     */
    public static void logList(String tag, List<String> list) {
        for (String str : list) {
            SimpleLogger.d(tag, str);
        }
    }

}
