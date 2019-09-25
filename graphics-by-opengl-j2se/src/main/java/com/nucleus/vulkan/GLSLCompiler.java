package com.nucleus.vulkan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.nucleus.SimpleLogger;
import com.nucleus.common.BufferUtils;
import com.nucleus.common.FileUtils;
import com.nucleus.common.Platform;
import com.nucleus.io.StreamUtils;
import com.nucleus.spirv.SpirvBinary;
import com.nucleus.spirv.SpirvLoader;

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
        ByteBuffer buffer = BufferUtils.createByteBuffer(16000);
        process = Platform.getInstance().executeCommand("cd c:\\", null, buffer);
        // File file = FileUtils.getInstance().getFile(path + "gltf/main_vert.spv");
        // SpirvLoader loader = new SpirvLoader();
        // loader.loadSpirv(new FileInputStream(file), buffer, 1000);

        compileStage(path, folders, buffer, Stage.vert);
        compileStage(path, folders, buffer, Stage.geom);
        compileStage(path, folders, buffer, Stage.frag);
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        process.destroy();
    }

    public void compileStage(String path, ArrayList<String> folders, ByteBuffer buffer, Stage stage)
            throws IOException {
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
                currentPath = setPath(currentPath, filepath, process, reader, buffer);

                name = filename.substring(0, filename.length() - (stage.name().length() + 1));
                output = name + "_" + stage.name() + ".spv";
                String cmd = "glslc " + filename + " -o -";
                Platform.getInstance().executeCommand(process, cmd, buffer);
                String str = StandardCharsets.ISO_8859_1.decode(buffer).toString();
                if (str.contains("error:") || str.contains("not recognized")) {
                    throw new IllegalArgumentException("Error compiling shader: \n" + str);
                } else {
                    SpirvLoader loader = new SpirvLoader();
                    loader.waitForMagic(process.getInputStream(), buffer, 1000);
                    Platform.getInstance().executeCommand(process, SpirvLoader.SPIRV_END_STR, buffer);
                    loader.loadSpirv(process.getInputStream(), buffer, 1000);
                    SpirvBinary spirv = new SpirvBinary(buffer, loader.getTotalWords());
                    SimpleLogger.d(getClass(), "Created SPIR-V with total words: " + spirv.totalWords);
                    StreamUtils.writeToStream(currentPath + output, spirv.getSpirv());
                }
            }
        }
    }

    private String setPath(String currentPath, String filepath, Process process, BufferedInputStream reader,
            ByteBuffer buffer) {
        if (currentPath == null || !currentPath.contentEquals(filepath)) {
            Platform.getInstance().executeCommand(process, "cd " + filepath, buffer);
            return filepath;
        }
        return currentPath;
    }

}
