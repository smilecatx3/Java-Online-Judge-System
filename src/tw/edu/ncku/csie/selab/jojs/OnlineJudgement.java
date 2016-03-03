package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.fileupload.FileItem;

import java.io.PrintWriter;
import java.io.Writer;

public class OnlineJudgement extends Judgement {
    public FileItem fileItem;
    private PrintWriter writer;

    public OnlineJudgement(String hwID, String studentID, ExecutionTask.Mode mode, FileItem fileItem, Writer writer) {
        super(hwID, studentID, mode);
        this.fileItem = fileItem;
        this.writer = new PrintWriter(writer, true);
    }

    @Override
    public void reportProgress(double progress, String message) {
        writer.println(String.format("<script> setProgress(%f, \"%s\"); </script>", progress, message));
    }
}
