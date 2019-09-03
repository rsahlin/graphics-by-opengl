package com.nucleus.common;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;

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
	 *         folder names)
	 */
	public String[] listResourceFolders(String path) {
		ClassLoader loader = getClass().getClassLoader();
		URL url = loader.getResource(path);
		if (url == null) {
			return new String[0];
		}
		File[] files = new File(url.getFile()).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		// Truncate name so only include the part after specified path.
		String[] folders = new String[files.length];
		int index = 0;
		path = path.replace('/', File.separatorChar);
		for (File f : files) {
			int start = f.toString().indexOf(path);
			folders[index++] = f.toString().substring(start + path.length());
		}
		return folders;
	}

	/**
	 * 
	 * @param path
	 * @param folders
	 * @param mime
	 * @return List of folder/filname for files that ends with mime
	 */
	public ArrayList<String> listFiles(String path, String[] folders, final String mime) {
		ArrayList<String> result = new ArrayList<>();
		ClassLoader loader = getClass().getClassLoader();
		String comparePath = path.replace('/', File.separatorChar);
		for (String folder : folders) {
			URL url = loader.getResource(path + folder);
			File[] files = new File(url.getFile()).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(mime);
				}
			});
			// Truncate name so only include the part after specified path.
			for (File f : files) {
				int start = f.toString().indexOf(comparePath);
				result.add(f.toString().substring(start + comparePath.length()));
			}
		}
		return result;
	}

}
