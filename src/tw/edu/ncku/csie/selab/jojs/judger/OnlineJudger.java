package tw.edu.ncku.csie.selab.jojs.judger;

import org.apache.commons.fileupload.FileItem;

import java.io.File;

public final class OnlineJudger extends Judger {
    private FileItem fileItem;

    public OnlineJudger(String hwID, String studentID, ProgressReporter reporter, FileItem fileItem) throws Exception {
        super(hwID, studentID, reporter);
        this.fileItem = fileItem;
    }

    @Override
    protected File getInputFile() throws Exception {
        File file = new File(workingDirectory, new File(fileItem.getName()).getName());
        fileItem.write(file);
        return file;
    }
}
