package tw.edu.ncku.csie.selab.jojs.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;

public class Executor {

    public static String execute(String[] command, File inputFile) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
        if (inputFile != null)
            processBuilder.redirectInput(inputFile);
        Process process = processBuilder.start();
        return IOUtils.toString(process.getInputStream(), "UTF-8");
    }

}
