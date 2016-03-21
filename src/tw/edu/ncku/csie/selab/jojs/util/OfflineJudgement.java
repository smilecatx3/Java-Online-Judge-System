package tw.edu.ncku.csie.selab.jojs.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import tw.edu.ncku.csie.selab.jojs.ExecutionResult;
import tw.edu.ncku.csie.selab.jojs.ExecutionTask;
import tw.edu.ncku.csie.selab.jojs.JOJS;
import tw.edu.ncku.csie.selab.jojs.JudgeException;
import tw.edu.ncku.csie.selab.jojs.JudgeResult;
import tw.edu.ncku.csie.selab.jojs.judger.OfflineJudger;

public class OfflineJudgement {

    public static void main(String args[]) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // Get judgement information
        if (args.length < 3) {
            System.err.println("Usage: java -jar jojs.jar {HW_ID} {MODE(standard|stdin)} {BASE_SCORE} [MOSS_USER_ID]");
            System.exit(0);
        }
        String hwID = args[0];
        ExecutionTask.Mode mode = ExecutionTask.Mode.parseMode(args[1]);
        int base = Integer.parseInt(args[2]);

        // Choose source directory
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        File folder = fileChooser.getSelectedFile();
        List<File> files = new ArrayList<>(FileUtils.listFiles(folder, new String[] {"zip"}, true));

        // Prepare outputs
        PrintStream out = new PrintStream(new TeeOutputStream(
                System.out,
                new FileOutputStream(new File(folder, String.format("log_%s.txt", hwID)))));
        File summary = new File(folder, String.format("summary_%s.csv", hwID));
        StringBuilder summaryBuilder = new StringBuilder("id,score,comment\n");

        // Start judgement
        try {
            List<File> validFiles = new ArrayList<>();
            for (int i=0; i<files.size(); i++) {
                File inputFile = files.get(i);
                String studentID = inputFile.getName().replace(".zip", "");
                int score = 0;
                StringBuilder comment = new StringBuilder();
                out.println(String.format("========== [%s] (%d/%d) ==========", studentID, i+1, files.size()));
                try {
                    Future<JudgeResult> future = JOJS.judge(new OfflineJudger(hwID, studentID, (progress, message)->{}, inputFile), mode);
                    JudgeResult judgeResult = future.get();
                    score = judgeResult.getScore(base);
                    ExecutionResult[] executionResults = judgeResult.getResults();
                    for (int j=0; j<executionResults.length; j++)
                        if (!executionResults[j].isPassed())
                            comment.append(j+1).append("; ");
                    validFiles.add(inputFile);
                } catch (Exception e) {
                    if (e.getCause() instanceof Exception)
                        e = (Exception) e.getCause();
                    if (e instanceof JudgeException) {
                        JudgeException ex = (JudgeException) e;
                        JudgeException.ErrorCode errorCode = ex.getErrorCode();
                        if (errorCode==JudgeException.ErrorCode.INVALID_STUDENT_ID) {
                            out.println(errorCode+"\n");
                            continue;
                        }
                        comment.append(errorCode);
                    } else {
                        e.printStackTrace(out);
                    }
                }
                summaryBuilder.append(studentID).append(",").append(score).append(",").append(comment).append("\n");
                out.println(String.format("%s => %d %s %n", studentID, score, (comment.length()>0) ? "/ "+comment : ""));
            }
            FileUtils.writeStringToFile(summary, summaryBuilder.toString());

            // Apply plagiarism detection
            if (args.length == 4) {
                PlagiarismDetector detector = new PlagiarismDetector(args[3], out);
                out.println("Results are available at " + detector.start(validFiles));
            }
        } catch (Exception e) {
            out.println(ExceptionUtils.getStackTrace(e));
        } finally {
            JOJS.executor.shutdownNow();
        }
    }

}
