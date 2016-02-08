package tw.edu.ncku.csie.selab.jojs;

public class ExecutionResult {
    private boolean passed;
    private String answer;

    public ExecutionResult(boolean passed, String answer) {
        this.passed = passed;
        this.answer = answer;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getAnswer() {
        return answer;
    }
}
