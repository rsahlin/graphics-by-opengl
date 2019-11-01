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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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

    FileSystem fileSystem;

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

    protected Path getFileSystemPath(String path) throws URISyntaxException, IOException {
        ClassLoader loader = getClass().getClassLoader();
        SimpleLogger.d(getClass(), "Getting URI for path: " + path);
        URI uri = loader.getResource(path).toURI();
        Path resultPath = null;
        if (uri.getScheme().contentEquals("jar")) {
            resultPath = getJarPath(uri, path);
        }
        resultPath = Paths.get(uri);
        SimpleLogger.d(getClass(), "Path for uri: " + uri + "\n" + resultPath.toString());
        return resultPath;
    }

    protected Path getJarPath(URI uri, String path) throws IOException {
        if (fileSystem == null) {
            fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object> emptyMap());
        }
        return fileSystem.getPath(path);
    }

    /**
     * Utility method to return a list with the folder in the specified resource
     * path
     * 
     * @param path List of subfolders of this path will be returned
     * @return folders in the specified path (excluding the path in the returned
     * folder names)
     */
    public ArrayList<String> listResourceFolders(String path) throws IOException, URISyntaxException {
        String separator = "" + FileUtils.DIRECTORY_SEPARATOR;
        ArrayList<String> folders = new ArrayList<String>();
        Path listPath = getFileSystemPath(path);
        String listPathStr = listPath.toString();
        SimpleLogger.d(getClass(), "Listing folders in " + listPathStr);
        int offset = listPathStr.endsWith(separator) ? 0 : 1;
        int len = listPathStr.length();
        try (Stream<Path> walk = Files.walk(listPath, 1)) {
            List<Path> result = walk.filter(Files::isDirectory)
                    .collect(Collectors.toList());
            for (Path folderPath : result) {
                String str = folderPath.toString();
                int strLen = str.length();
                if (strLen > len) {
                    int endoffset = str.endsWith(separator) ? 1 : 0;
                    String folder = str.substring(len + offset, strLen - endoffset);
                    SimpleLogger.d(getClass(), "Added folder: " + folder + ", fullpath: " + str);
                    folders.add(folder);
                }
            }
        }
        return folders;
    }

    /**
     * List the files, based on mime, beginning at path and including the specified folders.
     * 
     * @param path Base path to start file list - shall end with '/'
     * @param folders Folders to include in search
     * @param mimes File extensions to include
     * @return Matching files
     * @throws IOException
     * @throws URISyntaxException
     */
    public ArrayList<String> listFiles(String path, ArrayList<String> folders, final String[] mimes)
            throws URISyntaxException, IOException {
        ArrayList<String> result = new ArrayList<String>();
        for (String folder : folders) {
            Path listPath = getFileSystemPath(path + folder);
            SimpleLogger.d(getClass(), "Listing files in " + listPath.toString());
            try (Stream<Path> walk = Files.walk(listPath, 1)) {
                List<Path> filePathList = walk.filter(Files::isRegularFile)
                        .collect(Collectors.toList());
                String listStr = listPath.toString().replace('\\', FileUtils.DIRECTORY_SEPARATOR);
                int len = listStr.length();
                if (path.endsWith("/")) {
                    if (!listStr.endsWith("/")) {
                        listStr = listStr + "/";
                    }
                } else if (listStr.endsWith("/")) {
                    path = path + "/";
                }
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
