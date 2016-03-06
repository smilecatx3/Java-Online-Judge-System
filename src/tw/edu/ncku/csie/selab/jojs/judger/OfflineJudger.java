package tw.edu.ncku.csie.selab.jojs.judger;

import java.io.File;
import java.io.IOException;

import tw.edu.ncku.csie.selab.jojs.*;

public class OfflineJudger extends Judger {
    private File inputFile;

    public OfflineJudger(String hwID, String studentID, ProgressReporter reporter, File inputFile) throws JudgeException, IOException {
        super(hwID, studentID, reporter);
        this.inputFile = inputFile;
    }

    @Override
    protected File getInputFile() throws Exception {
        return inputFile;
    }
}
