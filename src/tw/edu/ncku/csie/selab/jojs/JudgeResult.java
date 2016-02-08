package tw.edu.ncku.csie.selab.jojs;

import org.json.JSONObject;

public class JudgeResult {
    private JSONObject testcase;
    private ExecutionResult[] results;
    private double runtime; // milliseconds
    private int score = 20; // Basic 20, total 100

    public JudgeResult(JSONObject testcase, ExecutionResult[] results, double runtime) {
        this.testcase = testcase;
        this.results = results;
        this.runtime = runtime;

        double sum = 0;
        double scorePerTestcase = (100.0 - score) / results.length;
        for (ExecutionResult result : results)
            sum += result.isPassed() ? scorePerTestcase : 0;
        this.score += Math.ceil(sum);
    }

    public JSONObject getTestcase() {
        return testcase;
    }

    public ExecutionResult[] getResults() {
        return results;
    }

    public double getRuntime() {
        return runtime;
    }

    public int getScore() {
        return score;
    }
}
