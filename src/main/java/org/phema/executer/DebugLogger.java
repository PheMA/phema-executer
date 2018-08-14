package org.phema.executer;

import org.phema.executer.exception.PhemaUserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Luke Rasmussen on 8/10/18.
 */
public class DebugLogger {
    public static final String LOGGING_DIRECTORY = "phema-cache";
    private static final String VERBOSE_LOGGING_FILE = "verbose-log.txt";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private File fullLoggingPath = null;
    private BufferedWriter verboseLoggingWriter = null;

    public DebugLogger() {
    }

    public void initialize(String basePath) throws Exception {
        File baseDir = new File(basePath);
        if (!baseDir.exists()) {
            throw new PhemaUserException(String.format("When attempting to enable debug logging, we tried to create a logging directory at %s, but this path could not be found (or you do not have access).",
                    basePath));
        }

        if (!baseDir.isDirectory()) {
            throw new PhemaUserException(String.format("When attempting to enable debug logging, a file path (%s) was used instead of a directory.  Please make sure the debug log path is only set to be a directory.",
                    basePath));
        }

        fullLoggingPath = (new File(baseDir, LOGGING_DIRECTORY));
        if (!fullLoggingPath.exists() && !fullLoggingPath.mkdir()) {
            throw new PhemaUserException(String.format("We were unable to create the debug logging directory at %s.  Please check your configuration, or disable debug logging", fullLoggingPath.getAbsolutePath()));
        }

        verboseLoggingWriter = new BufferedWriter(new FileWriter(new File(fullLoggingPath.getAbsolutePath(), VERBOSE_LOGGING_FILE), false));

        startLog();
    }

    public void startLog() throws IOException {
        writeLog("**************** STARTING DEBUG LOG ****************");
    }

    public void writeLog(String message) throws IOException {
        if (verboseLoggingWriter != null) {
            verboseLoggingWriter.write(String.format("%s - %s\r\n", dateFormat.format(new Date()), message));
            verboseLoggingWriter.flush();
        }
    }

    public void writeRaw(String message) throws IOException {
        if (verboseLoggingWriter != null) {
            verboseLoggingWriter.write(message);
            verboseLoggingWriter.write("\r\n");
            verboseLoggingWriter.flush();
        }
    }

    public void close() {
        try {
            if (verboseLoggingWriter != null) {
                verboseLoggingWriter.flush();
                verboseLoggingWriter.close();
                verboseLoggingWriter = null;
            }
        }
        catch (Exception exc) {
            // Eating the exception on purpose
        }
    }
}
