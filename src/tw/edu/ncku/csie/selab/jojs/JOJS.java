package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

public class JOJS {
    public static final JSONObject CONFIG;
    public static final String JAVA;

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
    }



    public static void main(String args[]) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        if (args.length != 2) {
            JOptionPane.showMessageDialog(null, "<html>Usage: <font style=\"font-family:Consolas;\">java -jar JOJS.jar <b>HW_ID</b> <b>MODE(standard|stdin)</b></font></html>");
            System.exit(0);
        }
        String hwID = args[0];
        ExecutionTask.Mode mode = ExecutionTask.Mode.parseMode(args[1]);

        // Choose source directory
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        File folder = fileChooser.getSelectedFile();
        File[] files = folder.listFiles();
        assert (files != null);

        File summary = new File(folder, String.format("summary_%s.csv", hwID));
        StringBuilder summaryBuilder = new StringBuilder("id,score,comment\n");
        for (int i=0; i<files.length; i++) {
            File file = files[i];
            System.out.println(String.format("========== [%s] (%d/%d) ==========", file.getName(), i+1, files.length));
            if (file.getName().endsWith(".zip")) {
                String studentID = file.getName().replace(".zip", "");
                Judger judger = new Judger(hwID, studentID);
                try {
                    judger.compile(file);
                    JudgeResult judgeResult = judger.execute(mode);
                    summaryBuilder.append(studentID).append(",").append(judgeResult.getScore()).append(",");
                    ExecutionResult[] executionResults = judgeResult.getResults();
                    for (int j=0; j<executionResults.length; j++)
                        if (!executionResults[j].isPassed())
                            summaryBuilder.append(j+1).append(" / ");
                    summaryBuilder.append("\n");
                    System.out.println(String.format("%s => %d", studentID, judgeResult.getScore()));
                } catch (JudgeException e) {
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                } finally {
                    judger.cleanWorkingDirectory();
                }
            } else {
                System.out.println("File name format is incorrect. Skip");
            }
            System.out.println();
        }
        FileUtils.writeStringToFile(summary, summaryBuilder.toString());
    }
}
