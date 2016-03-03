package tw.edu.ncku.csie.selab.jojs;

public abstract class Judgement {
    public String hwID;
    public String studentID;
    public ExecutionTask.Mode mode;

    protected Judgement(String hwID, String studentID, ExecutionTask.Mode mode) {
        this.hwID = hwID;
        this.studentID = studentID;
        this.mode = mode;
    }

    public abstract void reportProgress(double progress, String message);
}
