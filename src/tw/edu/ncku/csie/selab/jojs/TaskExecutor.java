package tw.edu.ncku.csie.selab.jojs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class TaskExecutor {

    public static String execute(String[] command) throws Exception {
        return execute(command, false);
    }

    public static String execute(String[] command, boolean useStdin) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
        if (useStdin)
            processBuilder.redirectInput(getRedirectInputFile(command));
        Process process = processBuilder.start();
        // Read process output
        try (BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = stdout.readLine()) != null)
                outputBuilder.append(line).append("\n");
            return outputBuilder.toString();
        }
    }

    private static File getRedirectInputFile(String[] inputs) throws Exception {
        File tempFile = File.createTempFile("stdin", ".temp");
        tempFile.deleteOnExit();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (String input : inputs) {
                writer.write(input);
                writer.newLine();
            }
        }
        return tempFile;
    }
}
