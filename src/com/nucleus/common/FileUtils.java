package com.nucleus.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Filebase utilities, reading and writing to/from file.
 * 
 * @author Richard Sahlin
 *
 */
public class FileUtils {

    /**
     * Reads from the inputstream using a BufferedReader and returns as a String.
     * 
     * @param is
     * @return The contents of the file as a String.
     */
    public static String readStringFromFile(InputStream is) throws IOException {
        StringBuffer result = new StringBuffer();
        Reader r = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(r);
        char[] buffer = new char[4096];
        int read = 0;
        while ((read = br.read(buffer)) != -1) {
            result.append(buffer, 0, read);
        }
        return result.toString();
    }

}
