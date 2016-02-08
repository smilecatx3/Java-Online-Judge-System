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

public class ExecutionTask implements Callable<JudgeResult> {
    private JSONObject testcase;
    private File binFolder;
    private String entryPoint;
    private boolean useStdin;

    public ExecutionTask(File testcase, File binFolder, String entryPoint, boolean useStdin) throws IOException {
        this.testcase = new JSONObject(FileUtils.readFileToString(testcase));
        this.binFolder = binFolder;
        this.entryPoint = entryPoint;
        this.useStdin = useStdin;
    }

    @Override
    public JudgeResult call() throws Exception {
        JSONArray inputs = testcase.getJSONArray("inputs");
        JSONArray outputs = testcase.getJSONArray("outputs");
        assert (inputs.length() == outputs.length()) : "The size of inputs and outputs differs";
        ExecutionResult[] results = new ExecutionResult[inputs.length()];

        double elapsedTime = 0;
        for (int i=0; i<inputs.length(); i++) {
            JSONArray input = inputs.getJSONArray(i);
            JSONArray output = outputs.getJSONArray(i);

            // Construct command
            List<String> command = new ArrayList<>();
            Collections.addAll(command, "java", "-cp", binFolder.getAbsolutePath(), "-Dfile.encoding=UTF-8", entryPoint);
            for (int j=0; j<input.length(); j++)
                command.add(input.getString(j));

            // Execute the program and record elapased time
            long startTime = System.currentTimeMillis();
            String answer = TaskExecutor.execute(command.toArray(new String[command.size()]), useStdin);
            elapsedTime += System.currentTimeMillis() - startTime;

            // Examine the output
            boolean passed = true;
            String[] linesOfAnswer = answer.trim().split("\n");
            if (linesOfAnswer.length != output.length()) {
                passed = false;
            } else {
                for (int j=0; j<output.length(); j++) {
                    if (!linesOfAnswer[j].trim().equals(output.getString(j).trim())) {
                        passed = false;
                        break;
                    }
                }
            }
            results[i] = new ExecutionResult(passed, answer);
        }
        return new JudgeResult(testcase, results, elapsedTime/results.length);
    }
}
