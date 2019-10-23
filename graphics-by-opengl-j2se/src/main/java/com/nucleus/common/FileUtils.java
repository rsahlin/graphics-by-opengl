package com.nucleus.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nucleus.SimpleLogger;
import com.nucleus.common.Platform.CommandResult;

/**
 * Utilities that are related to filesystem/file operations - singleton class to
 * use ClassLoader when needed.
 *
 */
public class FileUtils {

    protected static FileUtils fileUtils = null;
    public static char DIRECTORY_SEPARATOR = '/';

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

    protected Path getFileSystemPath(String path) throws URISyntaxException {
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource(path);
        String urlPath = url.getPath();
        if (urlPath.startsWith("file:")) {
            urlPath = urlPath.substring(5);
        }
        if (urlPath.startsWith("" + FileUtils.DIRECTORY_SEPARATOR)) {
            urlPath = urlPath.substring(1);
        }
        FileSystem fs = FileSystems.getFileSystem(new URI("file:///"));
        return fs.getPath(urlPath);
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
        try {
            Path listPath = getFileSystemPath(path);
            SimpleLogger.d(getClass(), "Listing folders in " + listPath.toString());
            try (Stream<Path> walk = Files.walk(listPath, 1)) {
                List<Path> result = walk.filter(Files::isDirectory)
                        .collect(Collectors.toList());
                int len = listPath.toString().length();
                for (Path folderPath : result) {
                    String str = folderPath.toString();
                    if (str.length() > len) {
                        folders.add(str.substring(len + 1));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return folders;
    }

    /**
     * List the files, based on mime, beginning at path and including the specified folders.
     * 
     * @param path Base path to start file list
     * @param folders Folders to include in search
     * @param mimes File extensions to include
     * @return Matching files
     */
    public ArrayList<String> listFiles(String path, ArrayList<String> folders, final String[] mimes) {
        ArrayList<String> result = new ArrayList<String>();
        for (String folder : folders) {
            try {
                Path listPath = getFileSystemPath(path + folder);
                SimpleLogger.d(getClass(), "Listing files in " + listPath.toString());
                try (Stream<Path> walk = Files.walk(listPath, 1)) {
                    List<Path> filePathList = walk.filter(Files::isRegularFile)
                            .collect(Collectors.toList());
                    String listStr = listPath.toString().replace('\\', FileUtils.DIRECTORY_SEPARATOR);
                    int len = listStr.length();
                    int relative = listStr.indexOf(path) + path.length();
                    if (relative < path.length()) {
                        throw new IllegalArgumentException("Could not find '" + path + "' in: " + listStr);
                    }
                    for (Path folderPath : filePathList) {
                        String str = folderPath.toString();
                        if (str.length() > len) {
                            str = str.substring(relative);
                            for (String mime : mimes) {
                                if (str.toLowerCase().endsWith(mime)) {
                                    result.add(str.replace('\\', FileUtils.DIRECTORY_SEPARATOR));
                                    break;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
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
     * Reads byte array from reader into buffer, at the position specified by result.read
     * 
     * @param reader
     * @param result
     * @param length Min number of bytes to read
     */
    public void readBuffer(BufferedInputStream reader, CommandResult result, int length) {
        int read = 0;
        try {
            while (read < length) {
                read += reader.read(result.result, result.read, result.result.length - result.read);
                if (read < length) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                    }
                }
            }
            result.read = read;
        } catch (IOException e) {
            SimpleLogger.d(getClass(), e.toString());
        }
    }

    /**
     * Waits for data to become available
     * 
     * @param in
     * @param timeoutMillis
     * @return Positive number means number of bytes found, 0 means timeout and -1 end of stream.
     */
    public int waitForAvailable(InputStream in, int timeoutMillis) {
        int len = -1;
        long start = System.currentTimeMillis();
        try {
            while ((len = in.available()) == 0 && (System.currentTimeMillis() - start) < timeoutMillis) {
                Thread.sleep(100);
            }
        } catch (InterruptedException | IOException e) {
            // Nothing to do
        }
        return len;
    }

    public URL getClassLocation(Class<?> theClass) {
        final String classLocation = theClass.getName().replace('.', DIRECTORY_SEPARATOR) + ".class";
        final ClassLoader loader = theClass.getClassLoader();
        if (loader == null) {
            return null;
        } else {
            return loader.getResource(classLocation);
        }
    }

}
