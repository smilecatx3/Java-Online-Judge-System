package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tw.edu.ncku.csie.selab.jojs.judger.Judger;
import tw.edu.ncku.csie.selab.jojs.judger.ProgressReporter;
import tw.edu.ncku.csie.selab.jojs.rule.RuleValidator;

public class ExecutionTask {
    private JSONObject testcase;
    private File binFolder;
    private String entryPoint;
    private Judger.Mode mode;
    private ProgressReporter reporter;


    public ExecutionTask(File testcase, File binFolder, String entryPoint, Judger.Mode mode, ProgressReporter reporter) throws IOException {
        this.testcase = new JSONObject(FileUtils.readFileToString(testcase));
        this.binFolder = binFolder;
        this.entryPoint = entryPoint;
        this.mode = mode;
        this.reporter = reporter;
    }

    public JudgeResult execute() throws Exception {
        JSONArray inputs = testcase.getJSONArray("inputs");
        JSONArray outputs = testcase.getJSONArray("outputs");
        long globalTimeout = testcase.has("timeout") ? testcase.getLong("timeout")*outputs.length() : JOJS.CONFIG.getLong("timeout");
        ProcessExecutor executor = testcase.has("timeout") ? new ProcessExecutor(testcase.getLong("timeout")) : new ProcessExecutor();
        int trimLevel = testcase.has("trim") ? testcase.getInt("trim") : JOJSConstants.TRIM_ALL;

        if (testcase.has("rules")) {
            RuleValidator validator = new RuleValidator(binFolder, testcase.getJSONArray("rules"));
            if (!validator.isValid())
                throw new JudgeException(validator.getRule(), JudgeException.ErrorCode.INVALID_CLASS);
        }

        double elapsedTime = 0;
        ExecutionResult[] results = new ExecutionResult[outputs.length()];
        for (int i=0; i<outputs.length(); i++) {
            reporter.reportProgress(0.5+((i+1.0)/outputs.length())*0.5, "Executing");

            // Construct command
            File inputFile = null; // stdin
            List<String> command = new ArrayList<>();
            Collections.addAll(command, JOJS.JAVA, "-Dfile.encoding=UTF-8", "-Duser.language=en", "-Djava.security.manager", "-cp", binFolder.getAbsolutePath(), entryPoint);
            if (inputs.length() > 0) {
                JSONArray input = inputs.getJSONArray(i);
                if (mode == Judger.Mode.STANDARD) {
                    for (int j = 0; j < input.length(); j++)
                        command.add(input.getString(j).replace("\"", "\\\""));
                } else if (mode == Judger.Mode.STDIN) {
                    inputFile = createInputFile(input);
                }
            }

            // Execute the program
            ProcessExecutor.Result result = executor.execute(command.toArray(new String[command.size()]), inputFile);
            if (result.isTimeout) {
                results[i] = new ExecutionResult(false, "Time limit exceeded");
            } else {
                String answer = result.output.replace("\r", ""); // Your output
                String output = outputs.getString(i).replace("\r", ""); // Expected output
                if (trimLevel == JOJSConstants.TRIM_ALL) {
                    answer = answer.trim();
                    output = output.trim();
                } else if (trimLevel == JOJSConstants.TRIM_LAST) {
                    answer = answer.replaceFirst("\\s+$", "");
                    output = output.replaceFirst("\\s+$", "");
                }
                boolean passed = answer.equals(output);
                results[i] = new ExecutionResult(passed, answer);
            }
            elapsedTime += result.runtime;

            if (elapsedTime >= globalTimeout)
                throw new JudgeException("Time limit exceeded", JudgeException.ErrorCode.TIME_LIMIT_EXCEEDED);
        }
        return new JudgeResult(testcase, results, elapsedTime/results.length);
    }

    // For stdin
    private File createInputFile(JSONArray input) throws IOException {
        StringBuilder data = new StringBuilder();
        for (int i=0; i<input.length(); i++)
            data.append(input.getString(i)).append("\n");
        File file = new File(binFolder, String.valueOf(System.currentTimeMillis()));
        FileUtils.writeStringToFile(file, data.toString());
        return file;
    }

}
