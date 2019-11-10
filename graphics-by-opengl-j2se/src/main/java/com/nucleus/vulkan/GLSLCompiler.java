package com.nucleus.vulkan;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.nucleus.SimpleLogger;
import com.nucleus.common.BufferUtils;
import com.nucleus.common.FileUtils;
import com.nucleus.common.Platform;
import com.nucleus.io.StreamUtils;
import com.nucleus.spirv.SpirvBinary;
import com.nucleus.spirv.SpirvBinary.SpirvStream;
import com.nucleus.vulkan.structs.ShaderModuleCreateInfo.Type;

/**
 * Used to compile GLSL to SPIR-V in runtime.
 * Singleton class.
 *
 */
public class GLSLCompiler {

    private static final String TARGET_CLASSES = "target/classes/";
    private static final String DESTINATION_RESOURCES = "src/main/resources/";

    private static GLSLCompiler compiler = new GLSLCompiler();

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
    public synchronized void compileShaders(String path, ArrayList<String> folders)
            throws IOException, URISyntaxException {
        ByteBuffer buffer = BufferUtils.createByteBuffer(16000);
        compileStage(path, folders, buffer, Type.VERTEX);
        compileStage(path, folders, buffer, Type.GEOMETRY);
        compileStage(path, folders, buffer, Type.FRAGMENT);
    }

    public void compileStage(String path, ArrayList<String> folders, ByteBuffer buffer, Type type)
            throws IOException, URISyntaxException {
        for (String folder : folders) {
            ArrayList<String> currentFolder = new ArrayList<String>();
            currentFolder.add(folder);
            // Get the mime for the shader type/stage - ie the glsl sourcefiles to compile
            String stageSuffix = "." + type.stage;
            ArrayList<String> filenames = FileUtils.getInstance().listFiles(path, currentFolder,
                    new String[] { stageSuffix });

            String name = null;
            String output = null;
            for (String filename : filenames) {
                String filePath = FileUtils.getInstance().getFilePath(path + filename, folder);
                name = filename.substring(0, filename.length() - (type.fileName.length() + 1));
                output = name + type.fileName;
                String cmd = "glslc " + filename + " -o -";
                buffer.clear();
                SpirvBinary binary = compile(
                        new String[] { filePath, cmd }, null, buffer);
                if (binary == null) {
                    throw new IllegalArgumentException("Error compiling shader: \n" + filePath);
                }
                String outPath = filePath.replace(TARGET_CLASSES, DESTINATION_RESOURCES);
                StreamUtils.writeToStream(outPath + output, binary.getSpirv());
                SimpleLogger.d(getClass(), "Written spirv to: " + outPath + output);
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
        Platform.getInstance().executeCommands(commands, buffer);
        SpirvStream stream = SpirvBinary.getStream(buffer);
        buffer.limit(stream.getOffset());
        SpirvBinary spirv = new SpirvBinary(buffer, stream.getOffset());
        SimpleLogger.d(getClass(), "Created SPIR-V with total words: " + spirv.totalWords);
        return spirv;
    }

}
