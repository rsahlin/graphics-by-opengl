package com.nucleus.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import com.nucleus.SimpleLogger;

/**
 * Utilities that are related to filesystem/file operations - singleton class to
 * use ClassLoader when needed.
 *
 */
public class FileUtils {

    protected static FileUtils fileUtils = null;

    /**
     * Hide the constructor
     */
    private FileUtils() {
    }

    public static FileUtils getInstance() {
        if (fileUtils == null) {
            fileUtils = new FileUtils();
        }
        return fileUtils;
    }

    /**
     * Utility method to return a list with the folder in the specified resource
     * path
     * 
     * @param path List of subfolders of this path will be returned
     * @return folders in the specified path (excluding the path in the returned
     * folder names)
     */
    public ArrayList<String> listResourceFolders(String path) {
        ArrayList<String> folders = new ArrayList<String>();
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource(path);
        if (url == null) {
            return folders;
        }
        File[] files = new File(url.getFile()).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        // Truncate name so only include the part after specified path.
        path = path.replace('/', File.separatorChar);
        for (File f : files) {
            int start = f.toString().indexOf(path);
            folders.add(f.toString().substring(start + path.length()));
        }
        return folders;
    }

    /**
     * List the files, based on mime, beginning at path and including the specified folders.
     * Returned filenames will be relative to path
     * 
     * @param path The base path, all returned names will be relative to this.
     * @param folders Folder to search - folder names will be included in returned name.
     * @param mime
     * @return List of folder/filename for files that ends with mime
     */
    public ArrayList<String> listFilesToString(String path, ArrayList<String> folders, final String[] mimes) {
        String comparePath = path.replace('/', File.separatorChar);
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<File> files = listFilesToFile(path, folders, mimes);
        // Truncate name so only include the part after specified path.
        for (File f : files) {
            int start = f.toString().indexOf(comparePath);
            result.add(f.toString().substring(start + comparePath.length()));
        }
        return result;
    }

    /**
     * List the files, based on mime, beginning at path and including the specified folders.
     * 
     * @param path Base path to start file list
     * @param folders Folders to include in search
     * @param mimes File extensions to include
     * @return Matching files
     */
    public ArrayList<File> listFilesToFile(String path, ArrayList<String> folders, final String[] mimes) {
        ClassLoader loader = getClass().getClassLoader();
        ArrayList<File> result = new ArrayList<File>();
        for (String folder : folders) {
            URL url = loader.getResource(path + folder);
            File[] files = new File(url.getFile()).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    for (String mime : mimes) {
                        if (name.endsWith(mime)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            for (File f : files) {
                result.add(f);
            }
        }
        return result;

    }

    /**
     * Returns the File for the filename - or null if not found
     * 
     * @param filename
     * @return
     */
    public File getFile(String filename) {
        ClassLoader loader = getClass().getClassLoader();
        try {
            return new File(new URI(loader.getResource(filename).toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads string from reader into buffer and returns as String.
     * 
     * @param reader
     * @param buffer Buffer to read data into, String converted from this. If null a buffer with size 1024 will be
     * created.
     * @param index
     * @param length
     * @return String read from reader. Length of string is number of chars read.
     */
    public String readString(BufferedReader reader, char[] buffer, int index, int length) {
        try {
            char[] useBuffer = buffer;
            if (useBuffer == null) {
                useBuffer = new char[1024];
                index = 0;
                length = useBuffer.length;
            }
            int read = 0;
            read = reader.read(useBuffer, index, length);
            String str = new String(useBuffer, index, read);
            return str;
        } catch (IOException e) {
            SimpleLogger.d(getClass(), e.toString());
            return "";
        }
    }

}
