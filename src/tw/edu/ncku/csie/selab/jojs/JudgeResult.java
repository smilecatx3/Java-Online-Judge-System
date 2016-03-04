package tw.edu.ncku.csie.selab.jojs;

import org.json.JSONObject;

public class JudgeResult {
    private JSONObject testcase;
    private ExecutionResult[] results;
    private long runtime; // milliseconds
    private int numPassed;

    public JudgeResult(JSONObject testcase, ExecutionResult[] results, double runtime) {
        this.testcase = testcase;
        this.results = results;
        this.runtime = (long) runtime;
        for (ExecutionResult result : results)
            numPassed += result.isPassed() ? 1 : 0;
    }

    public JSONObject getTestcase() {
        return testcase;
    }

    public ExecutionResult[] getResults() {
        return results;
    }

    public long getRuntime() {
        return runtime;
    }

    public int getNumPassed() {
        return numPassed;
    }

    public int getScore(int base) {
        if (base < 0 || base > 100)
            throw new IllegalArgumentException("base should be in the range [0, 100]");
        return (int)Math.ceil(base + ((double)numPassed/results.length)*(100-base));
    }
}
