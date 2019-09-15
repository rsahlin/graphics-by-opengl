package com.nucleus.vulkan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.nucleus.common.FileUtils;
import com.nucleus.common.Platform;
import com.nucleus.common.Platform.CommandResult;
import com.nucleus.io.StreamUtils;
import com.nucleus.spirv.SpirvBinary;

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
    private BufferedInputStream reader;
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
     * @throws IOException
     */
    public synchronized void compileShaders(String path, ArrayList<String> folders) throws IOException {
        byte[] buffer = new byte[8000];
        CommandResult result = new CommandResult(buffer);
        process = Platform.getInstance().executeCommand("cd c:\\", null, result);
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

    public void compileStage(String path, ArrayList<String> folders, Stage stage) throws IOException {
        byte[] buffer = new byte[16000];
        CommandResult result = new CommandResult(buffer);
        for (String folder : folders) {
            ArrayList<String> currentFolder = new ArrayList<String>();
            currentFolder.add(folder);
            String stageSuffix = "." + stage.name();
            ArrayList<String> filenames = FileUtils.getInstance().listFilesToString(path, currentFolder,
                    new String[] { stageSuffix });

            String name = null;
            String output = null;
            for (String filename : filenames) {
                File file = FileUtils.getInstance().getFile(path + filename);
                String filepath = file.getPath().substring(0,
                        file.getPath().indexOf(folder.length() > 0 ? folder + "\\" + file.getName() : file.getName()));
                currentPath = setPath(currentPath, filepath, process, reader, result);

                name = filename.substring(0, filename.length() - (stage.name().length() + 1));
                output = name + "_" + stage.name() + ".spv";
                String cmd = "glslc " + filename + " -o -";
                Platform.getInstance().executeCommand(process, cmd, result);
                String str = new String(result.result, 0, result.read);
                if (str.contains("error:")) {
                    throw new IllegalArgumentException("Error compiling shader: \n" + result);
                } else {
                    if (str.trim().length() == cmd.length()) {
                        // Reset number of bytes read to force read
                        result.read = 0;
                    }
                    // Find spir-v magic number
                    while (!SpirvBinary.hasSPIRVMagic(result.result, 0, result.read)) {
                        FileUtils.getInstance().readBuffer(new BufferedInputStream(process.getInputStream()), result,
                                4);
                    }
                    SpirvBinary spirv = new SpirvBinary(result.result);
                    StreamUtils.writeToStream(currentPath + output, spirv.getSpirv());
                }
            }
        }
    }

    private String setPath(String currentPath, String filepath, Process process, BufferedInputStream reader,
            CommandResult result) {
        if (currentPath == null || !currentPath.contentEquals(filepath)) {
            Platform.getInstance().executeCommand(process, "cd " + filepath, result);
            return filepath;
        }
        return currentPath;
    }

}
