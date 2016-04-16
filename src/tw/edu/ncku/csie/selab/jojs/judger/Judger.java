package tw.edu.ncku.csie.selab.jojs.judger;

import net.lingala.zip4j.core.ZipFile;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import tw.edu.ncku.csie.selab.jojs.CompilationTask;
import tw.edu.ncku.csie.selab.jojs.ExecutionTask;
import tw.edu.ncku.csie.selab.jojs.JOJS;
import tw.edu.ncku.csie.selab.jojs.JudgeException;
import tw.edu.ncku.csie.selab.jojs.JudgeResult;

public abstract class Judger {
    public enum Mode { STANDARD, STDIN }

    private long judgeTime;
    private String hwID, studentID;
    private File srcFolder, binFolder;
    private String entryPoint;
    private ProgressReporter reporter;
    protected File workingDirectory;


    protected Judger(String hwID, String studentID, ProgressReporter reporter) throws JudgeException {
        if (!studentID.matches("[A-Z][0-9]{8}"))
            throw new JudgeException("The student ID is invalid: " + studentID, JudgeException.ErrorCode.INVALID_STUDENT_ID);

        this.hwID = hwID;
        this.studentID = studentID;
        this.reporter = reporter;
    }

    public JudgeResult judge(Mode mode) throws Exception {
        judgeTime = System.currentTimeMillis();

        // Create working directorty
        if (JOJS.CONFIG.getString("working_dir").equals("auto"))
            workingDirectory = new File(FileUtils.getTempDirectory(), String.format("jojs/%s/%s_%d", hwID, studentID, judgeTime));
        else
            workingDirectory = new File(JOJS.CONFIG.getString("working_dir"), String.format("jojs/%s/%s_%d", hwID, studentID, judgeTime));
        srcFolder = new File(workingDirectory, "src");
        binFolder = new File(workingDirectory, "bin");
        FileUtils.forceMkdir(binFolder);
        System.out.println(String.format("[%s] Working directory: %s",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).format(judgeTime),
                workingDirectory));

        validateInput();

        // Compile
        reporter.reportProgress(0.5, "Compiling");
        new CompilationTask(srcFolder, binFolder).execute();

        // Execute
        File testcase = new File(JOJS.CONFIG.getString("testcase_dir"), hwID+".json");
        return new ExecutionTask(testcase, binFolder, entryPoint, mode, reporter).execute();
    }

    public long getJudgeTime() {
        return judgeTime;
    }

    public void clean() {
        try {
            if (workingDirectory.exists())
                FileUtils.forceDelete(workingDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void validateInput() throws Exception {
        // Unzip source code
        File inputFile = getInputFile();
        if (!inputFile.getName().endsWith(String.format("%s.zip", studentID)))
            throw new JudgeException(printInputFormat(), JudgeException.ErrorCode.INVALID_INPUT);
        new ZipFile(inputFile).extractAll(workingDirectory.getAbsolutePath());
        if (!srcFolder.exists())
            throw new JudgeException(printInputFormat(), JudgeException.ErrorCode.INVALID_INPUT);

        // Get main class (entry point) to execute
        File manifest = new File(workingDirectory, "META-INF/MANIFEST.MF");
        if (!manifest.exists())
            throw new JudgeException(printInputFormat(), JudgeException.ErrorCode.INVALID_INPUT);
        for (String line : FileUtils.readFileToString(manifest).replace("\r", "").split("\n"))
            if (line.matches("(Main-Class:)\\s*.+"))
                entryPoint = line.replace("Main-Class:", "").trim();
        if (entryPoint == null)
            throw new JudgeException("The Main-Class is not specified.", JudgeException.ErrorCode.NO_MAIN_CLASS);
    }

    private String printInputFormat() {
        return String.format("The input file should be \"%s.zip\" with the following structure.\n\n", studentID) +
                String.format("%s.zip \n", studentID) +
                "|- src \n" +
                "|- META-INF \n" +
                "|  |- MANIFEST.MF \n";
    }

    protected abstract File getInputFile() throws Exception;
}
