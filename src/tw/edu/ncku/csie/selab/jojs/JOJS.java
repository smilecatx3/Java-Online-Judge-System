package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import tw.edu.ncku.csie.selab.jojs.judger.Judger;
import tw.edu.ncku.csie.selab.jojs.judger.OfflineJudger;

public class JOJS {
    public static final JSONObject CONFIG;
    public static final String JAVA;
    public static ExecutorService executor;

    static {
        JSONObject temp = null;
        try {
            temp = new JSONObject(IOUtils.toString(JOJS.class.getResourceAsStream("/data/config.json")));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        CONFIG = temp;
        JAVA = CONFIG.getString("java");
        executor = Executors.newFixedThreadPool(CONFIG.getInt("max_thread"));
    }

    public synchronized static Future<JudgeResult> judge(Judger judger, ExecutionTask.Mode mode) throws JudgeException, ExecutionException, InterruptedException {
        return executor.submit(() -> {
            try {
                judger.compile();
                return judger.execute(mode);
            } finally {
                judger.clean();
            }
        });
    }



    // TODO add log file
    public static void main(String args[]) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        if (args.length != 3) {
            System.err.println("Usage: java -jar jojs.jar {HW_ID} {MODE(standard|stdin)} {BASE_SCORE}");
            System.exit(0);
        }
        String hwID = args[0];
        ExecutionTask.Mode mode = ExecutionTask.Mode.parseMode(args[1]);
        int score = Integer.parseInt(args[2]);

        // Choose source directory
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        File folder = fileChooser.getSelectedFile();
        List<File> files = new ArrayList<>(FileUtils.listFiles(folder, new String[] {"zip"}, true));

        File summary = new File(folder, String.format("summary_%s.csv", hwID));
        StringBuilder summaryBuilder = new StringBuilder("id,score,comment\n");
        for (int i=0; i<files.size(); i++) {
            File file = files.get(i);
            String studentID = file.getName().replace(".zip", "");
            StringBuilder comment = new StringBuilder();
            System.out.println(String.format("========== [%s] (%d/%d) ==========", studentID, i+1, files.size()));
            try {
                Future<JudgeResult> future = JOJS.judge(new OfflineJudger(hwID, studentID, (progress, message)->{}, file), mode);
                JudgeResult judgeResult = future.get();
                score = judgeResult.getScore(score);
                ExecutionResult[] executionResults = judgeResult.getResults();
                for (int j=0; j<executionResults.length; j++)
                    if (!executionResults[j].isPassed())
                        comment.append(j+1).append("; ");
            } catch (Exception e) {
                if (e.getCause() instanceof Exception)
                    e = (Exception) e.getCause();
                if (e instanceof JudgeException) {
                    JudgeException ex = (JudgeException) e;
                    JudgeException.ErrorCode errorCode = ex.getErrorCode();
                    if (errorCode==JudgeException.ErrorCode.INVALID_STUDENT_ID) {
                        System.out.println(errorCode+"\n");
                        continue;
                    }
                    comment.append(errorCode);
                } else {
                    e.printStackTrace(System.out);
                }
            }
            summaryBuilder.append(studentID).append(",").append(score).append(",").append(comment).append("\n");
            System.out.println(String.format("%s => %d %s %n", studentID, score, (comment.length()>0) ? "/ "+comment : ""));
        }
        FileUtils.writeStringToFile(summary, summaryBuilder.toString());
        executor.shutdownNow();
        System.exit(0);
    }
}
