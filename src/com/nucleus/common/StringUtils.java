package com.nucleus.common;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class StringUtils {

    protected final static String DEFAULT_DELIMITER = ",";

    /**
     * Returns the string as a String[], delimetered by ','
     * 
     * @param str
     * @return
     */
    public static String[] getStringArray(String str) {
        ArrayList<String> resultList = getArrayList(str, DEFAULT_DELIMITER);
        String[] result = new String[resultList.size()];
        int index = 0;
        for (String s : resultList) {
            result[index++] = s;
        }
        return result;
    }

    public static ArrayList<String> getArrayList(String str, String delimiter) {
        ArrayList<String> resultList = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreTokens()) {
            resultList.add(st.nextToken());
        }
        return resultList;
    }

    public static float[] getFloatArray(String str) {
        ArrayList<String> resultList = getArrayList(str, DEFAULT_DELIMITER);
        float[] result = new float[resultList.size()];
        int index = 0;
        for (String s : resultList) {
            result[index++] = Float.parseFloat(s);
        }
        return result;
    }

    public static int[] getIntArray(String str) {
        ArrayList<String> resultList = getArrayList(str, DEFAULT_DELIMITER);
        int[] result = new int[resultList.size()];
        int index = 0;
        for (String s : resultList) {
            result[index++] = Integer.parseInt(s);
        }
        return result;
    }

    public static short[] getShortArray(String str) {
        ArrayList<String> resultList = getArrayList(str, DEFAULT_DELIMITER);
        short[] result = new short[resultList.size()];
        int index = 0;
        for (String s : resultList) {
            result[index++] = Short.parseShort(s);
        }
        return result;
    }
}
