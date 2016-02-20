package tw.edu.ncku.csie.selab.jojs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Executor {

    public static String execute(String[] command, File inputFile) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
        if (inputFile != null)
            processBuilder.redirectInput(inputFile);
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
}
