package com.nucleus.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;

import com.nucleus.SimpleLogger;
import com.nucleus.io.StreamUtils;

public class Platform {

    public static final String OS_NAME = "os.name";
    public static final String JAVA_VENDOR = "java.vendor";

    public enum OS {
        windows(0),
        linux(1),
        macos(2),
        android(3),
        unknown(4);

        public final int index;

        private OS(int index) {
            this.index = index;
        }

        public static OS getOS(String osName, String vendor) {
            if (osName == null) {
                return null;
            }
            if (osName.toLowerCase().contains("windows")) {
                return windows;
            }
            if (osName.toLowerCase().contains("mac")) {
                return macos;
            }
            if (osName.toLowerCase().contains("linux")) {
                if (vendor.toLowerCase().contains("android")) {
                    return android;
                }
                return linux;
            }
            return unknown;
        }

    }

    public static class CommandResult {
        public CommandResult(byte[] result) {
            this.result = result;
        }

        public final byte[] result;
        public int read;
    }

    private final String[] COMMAND = new String[] { "cmd.exe", "/bin/bash", "/bin/bash", "/bin/bash",
            "/bin/bash" };
    private final String[] EXIT = new String[] { "exit", "exit", "exit", "exit", };

    private static Platform instance;
    private OS os;

    private Platform() {
        String osName = System.getProperty(OS_NAME);
        String vendor = System.getProperty(JAVA_VENDOR, "");
        os = OS.getOS(osName, vendor);
    }

    /**
     * Returns the singleton platform instance
     * 
     * @return
     */
    public static Platform getInstance() {
        if (instance == null) {
            instance = new Platform();
        }
        return instance;
    }

    public OS getOS() {
        return os;
    }

    /**
     * Starts a new command process.
     * 
     * @param destination
     * @param The buffer to store output from executed command, data is stored at beginning of buffer
     * @return The process to send more commands to or read input from - must be terminated by caller.
     */
    public Process startProcess(Redirect destination, ByteBuffer buffer) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(COMMAND[os.index]);
        builder.redirectErrorStream(true);
        try {
            if (destination != null) {
                builder.redirectInput(destination);
            }
            Process process = builder.start();
            readFromStream(process.getInputStream(), buffer);
            return process;
        } catch (IOException e) {
            SimpleLogger.d(getClass(), "Could not start execute process");
            e.printStackTrace();
        }
        return null;
    }

    public int endProcess(Process process, ByteBuffer buffer) {
        executeCommand(process, EXIT[os.index], buffer);
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;

    }

    public ByteBuffer executeCommand(Process process, String command, ByteBuffer buffer) {
        BufferedWriter pWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        try {
            pWriter.write(command);
            pWriter.newLine();
            pWriter.flush();
            readFromStream(process.getInputStream(), buffer);
            return buffer;
        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    private int readFromStream(InputStream in, ByteBuffer buffer) throws IOException {
        int position = buffer.position();
        int len = FileUtils.getInstance().waitForAvailable(in, 1000);
        int read = StreamUtils.readFromStream(in, buffer, len);
        buffer.limit(buffer.position());
        buffer.position(position);
        // SimpleLogger.d(getClass(), "Output from starting command:\n" + StandardCharsets.ISO_8859_1.decode(buffer));
        return read;
    }

}
