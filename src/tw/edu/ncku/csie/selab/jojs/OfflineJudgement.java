package tw.edu.ncku.csie.selab.jojs;

import java.io.File;

public class OfflineJudgement extends Judgement {
    public File file;

    public OfflineJudgement(String hwID, String studentID, ExecutionTask.Mode mode, File file) {
        super(hwID, studentID, mode);
        this.file = file;
    }

    @Override
    public void reportProgress(double progress, String message) {
        System.out.println(message);
    }
}
