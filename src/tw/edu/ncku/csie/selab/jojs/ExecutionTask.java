package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import tw.edu.ncku.csie.selab.jojs.judger.ProgressReporter;
import tw.edu.ncku.csie.selab.jojs.util.Executor;

public class ExecutionTask implements Callable<JudgeResult> {
    public enum Mode {
        STANDARD, STDIN;

        public static Mode parseMode(String string) {
            switch (string.toLowerCase()) {
                case "standard": return STANDARD;
                case "stdin" : return STDIN;
                default: return null;
            }
        }
    }

    private JSONObject testcase;
    private File binFolder;
    private String entryPoint;
    private Mode mode;
    private ProgressReporter reporter;

    public ExecutionTask(File testcase, File binFolder, String entryPoint, Mode mode, ProgressReporter reporter) throws IOException {
        this.testcase = new JSONObject(FileUtils.readFileToString(testcase));
        this.binFolder = binFolder;
        this.entryPoint = entryPoint;
        this.mode = mode;
        this.reporter = reporter;
    }

    @Override
    public JudgeResult call() throws IOException {
        JSONArray inputs = testcase.getJSONArray("inputs");
        JSONArray outputs = testcase.getJSONArray("outputs");
        ExecutionResult[] results = new ExecutionResult[outputs.length()];

        double elapsedTime = 0;
        for (int i=0; i<outputs.length(); i++) {
            reporter.reportProgress(0.5+((i+1.0)/outputs.length())*0.5, "Executing");

            // Construct command
            File inputFile = null; // stdin
            List<String> command = new ArrayList<>();
            Collections.addAll(command, JOJS.JAVA, "-Dfile.encoding=UTF-8", "-Duser.language=en", "-Djava.security.manager", "-cp", binFolder.getAbsolutePath(), entryPoint);
            if (inputs.length() > 0) {
                JSONArray input = inputs.getJSONArray(i);
                if (mode == Mode.STANDARD) {
                    for (int j = 0; j < input.length(); j++)
                        command.add(input.getString(j).replace("\"", "\\\""));
                } else if (mode == Mode.STDIN) {
                    inputFile = createInputFile(input);
                }
            }

            // Execute the program and record elapased time
            long startTime = System.currentTimeMillis();
            String answer = Executor.execute(command.toArray(new String[command.size()]), inputFile);
            elapsedTime += System.currentTimeMillis() - startTime;

            // Examine the output
            String output = outputs.getString(i).replace("\r", "").trim();
            answer = answer.replace("\r", "").trim();
            boolean passed = answer.equals(output);
            results[i] = new ExecutionResult(passed, answer);
        }
        return new JudgeResult(testcase, results, elapsedTime/results.length);
    }

    private File createInputFile(JSONArray input) throws IOException {
        StringBuilder data = new StringBuilder();
        for (int i=0; i<input.length(); i++)
            data.append(input.getString(i)).append("\n");
        File file = new File(binFolder, String.valueOf(System.currentTimeMillis()));
        FileUtils.writeStringToFile(file, data.toString());
        return file;
    }
}
