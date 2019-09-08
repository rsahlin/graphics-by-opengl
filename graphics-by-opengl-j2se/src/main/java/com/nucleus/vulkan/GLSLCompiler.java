package com.nucleus.vulkan;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.nucleus.SimpleLogger;
import com.nucleus.common.FileUtils;
import com.nucleus.common.Platform;

/**
 * Used to compile GLSL to SPIR-V in runtime.
 * Singleton class.
 *
 */
public class GLSLCompiler {

    public enum Stage {
        vert(),
        tesc(),
        tese(),
        geom(),
        frag(),
        comp();

    }

    private static GLSLCompiler compiler = new GLSLCompiler();
    private Process process;
    private BufferedReader reader;
    private String currentPath = null;

    /**
     * Returns the glsl compiler instance used to compile GLSL into spir-v
     * 
     * @return
     */
    public static GLSLCompiler getInstance() {
        if (compiler == null) {
            compiler = new GLSLCompiler();
        }
        return compiler;
    }

    /**
     * Compiles the shaders found in path + folders into spir-v
     * Not threadsafe - only call from one thread.
     * 
     * @param path
     * @param folders
     */
    public synchronized void compileShaders(String path, ArrayList<String> folders) {
        process = Platform.getInstance().executeCommand("cd c:\\", null);
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        char[] buffer = new char[4000];
        SimpleLogger.d(getClass(), "Output from starting command process:\n"
                + FileUtils.getInstance().readString(reader, buffer, 0, buffer.length));
        compileStage(path, folders, Stage.vert);
        compileStage(path, folders, Stage.geom);
        compileStage(path, folders, Stage.frag);
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        process.destroy();
    }

    public void compileStage(String path, ArrayList<String> folders, Stage stage) {
        for (String folder : folders) {
            ArrayList<String> currentFolder = new ArrayList<String>();
            currentFolder.add(folder);
            ArrayList<String> filenames = FileUtils.getInstance().listFilesToString(path, currentFolder,
                    new String[] { "." + stage.name() });

            String name = null;
            String output = null;
            for (String filename : filenames) {
                File file = FileUtils.getInstance().getFile(path + filename);
                String filepath = file.getPath().substring(0,
                        file.getPath().indexOf(folder.length() > 0 ? folder + "\\" + file.getName() : file.getName()));
                currentPath = setPath(currentPath, filepath, process, reader);

                name = filename.substring(0, filename.length() - (stage.name().length() + 1));
                output = name + "_" + stage.name() + ".spv";
                String cmd = "glslc " + filename + " -o " + output;
                SimpleLogger.d(getClass(), "Compiling " + filename + " to " + output + ", using: '" + "glslc "
                        + filename + " -o " + output + "'");
                Platform.getInstance().executeCommand(process, cmd);
                String result = FileUtils.getInstance().readString(reader, null, 0, -1);
                if (result.contains("error:")) {
                    throw new IllegalArgumentException("Error compiling shader: \n" + result);
                }
            }
        }

    }

    private String setPath(String currentPath, String filepath, Process process, BufferedReader reader) {
        if (currentPath == null || !currentPath.contentEquals(filepath)) {
            SimpleLogger.d(getClass(), "Setting path.");
            Platform.getInstance().executeCommand(process, "cd " + filepath);
            SimpleLogger.d(getClass(), FileUtils.getInstance().readString(reader, null, 0, -1));
            return filepath;
        }
        return currentPath;
    }

}
