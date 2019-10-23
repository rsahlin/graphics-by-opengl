package com.nucleus.vulkan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.nucleus.SimpleLogger;
import com.nucleus.common.BufferUtils;
import com.nucleus.common.FileUtils;
import com.nucleus.common.Platform;
import com.nucleus.io.StreamUtils;
import com.nucleus.spirv.SpirvBinary;
import com.nucleus.spirv.SpirvBinary.SpirvStream;
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
            ArrayList<String> filenames = FileUtils.getInstance().listFiles(path, currentFolder,
                    new String[] { stageSuffix });

            String name = null;
            String output = null;
            for (String filename : filenames) {
                File file = FileUtils.getInstance().getFile(path + filename);
                String filePath = file.getPath().substring(0,
                        file.getPath().indexOf(folder.length() > 0 ? folder + "\\" + file.getName() : file.getName()));
                name = filename.substring(0, filename.length() - (stage.name().length() + 1));
                output = name + "_" + stage.name() + ".spv";
                String cmd = "glslc " + filename + " -o - & echo " + SpirvBinary.SPIRV_END_MARKER;
                buffer.clear();
                SpirvBinary binary = compile(new String[] { "cd " + filePath, cmd }, null, buffer);
                if (binary == null) {
                    throw new IllegalArgumentException("Error compiling shader: \n" + filePath);
                }
                StreamUtils.writeToStream(filePath + output, binary.getSpirv());
            }
        }
    }

    /**
     * Starts a new command process.
     * Executes the command and returns the process - only call this if a process has not been started before.
     * 
     * @param command
     * @param destination
     * @param The buffer to store output from executed command, data is stored at beginning of buffer
     * @return The spirv binary or null if failed
     */
    public SpirvBinary compile(String[] commands, Redirect destination, ByteBuffer buffer) {
        try {
            Process process = Platform.getInstance().startProcess(null, buffer);
            for (String cmd : commands) {
                Platform.getInstance().executeCommand(process, cmd, buffer);
            }
            String str = StandardCharsets.ISO_8859_1.decode(buffer).toString();
            if (str.contains("error:") || str.contains("not recognized")) {
                throw new IllegalArgumentException("Error compiling shader: \n" + str);
            }
            int result = Platform.getInstance().endProcess(process, buffer);
            // process.destroy();
            if (result != 0) {
                throw new IllegalArgumentException("Exception executing command, exitcode " + result);
            }
            int read = StreamUtils.readFromStream(process.getInputStream(), buffer, -1);
            buffer.rewind();
            SpirvLoader loader = new SpirvLoader();
            SpirvStream stream = loader.loadSpirv(process.getInputStream(), buffer, 1000);
            buffer.limit(stream.getOffset());
            SpirvBinary spirv = new SpirvBinary(buffer, stream.getOffset());
            SimpleLogger.d(getClass(), "Created SPIR-V with total words: " + spirv.totalWords);
            return spirv;
        } catch (IOException e) {
            SimpleLogger.d(getClass(), "Could not start execute process");
            e.printStackTrace();
        }
        return null;
    }

}
