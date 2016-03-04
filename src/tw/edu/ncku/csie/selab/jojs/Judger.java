package tw.edu.ncku.csie.selab.jojs;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Judger {
    private static final File TESTCASE_DIR = new File(JOJS.CONFIG.getString("testcase_dir"));
    private static final int TIMEOUT = JOJS.CONFIG.getInt("exec_timeout");
    private String hwID, studentID;
    private File workingDirectory;
    private File srcFolder, binFolder;

    Judger(String hwID, String studentID) throws JudgeException, IOException {
        if (!studentID.matches("[A-Z][0-9]{8}"))
            throw new JudgeException("The student ID is invalid: " + studentID, JudgeException.ErrorCode.INVALID_STUDENT_ID);

        this.hwID = hwID;
        this.studentID = studentID;

        // Create working directorty
        workingDirectory = new File(FileUtils.getTempDirectory(), String.format("jojs/%s/%s_%d", hwID, studentID, System.currentTimeMillis()));
        srcFolder = new File(workingDirectory, "src");
        binFolder = new File(workingDirectory, "bin");

        cleanWorkingDirectory();
        FileUtils.forceMkdir(binFolder);

        System.out.println("Working directory: " + workingDirectory);
    }

    public void compile(File zipFile) throws JudgeException, IOException, ZipException {
        // Unzip source code
        if (!zipFile.getName().endsWith(String.format("%s.zip", studentID)))
            throw new JudgeException(printInputFormat(), JudgeException.ErrorCode.INVALID_INPUT);
        new ZipFile(zipFile).extractAll(workingDirectory.getAbsolutePath());
        if (!srcFolder.exists())
            throw new JudgeException(printInputFormat(), JudgeException.ErrorCode.INVALID_INPUT);

        // Compile
        new CompilationTask(srcFolder, binFolder).execute();
    }

    public JudgeResult execute(Judgement judgement) throws JudgeException, IOException, ExecutionException, InterruptedException {
        // Get main class (entry point) to execute
        File manifest = new File(workingDirectory, "META-INF/MANIFEST.MF");
        if (!manifest.exists())
            throw new JudgeException(printInputFormat(), JudgeException.ErrorCode.INVALID_INPUT);
        String entryPoint = null;
        for (String line : FileUtils.readFileToString(manifest).replace("\r", "").split("\n"))
            if (line.matches("(Main-Class:)\\s*.+"))
                entryPoint = line.replace("Main-Class:", "").trim();
        if (entryPoint == null)
            throw new JudgeException("The Main-Class is not specified.", JudgeException.ErrorCode.NO_MAIN_CLASS);

        // Execute the program with time limit
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<JudgeResult> future = executor.submit(new ExecutionTask(new File(TESTCASE_DIR, hwID+".json"), binFolder, entryPoint, judgement));
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new JudgeException(String.format("Your program exceeded time limit %d seconds.", TIMEOUT), JudgeException.ErrorCode.TIME_LIMIT_EXCEEDED);
        } finally {
            executor.shutdownNow();
        }
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void cleanWorkingDirectory() {
        try {
            if (workingDirectory.exists())
                FileUtils.forceDelete(workingDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String printInputFormat() {
        return String.format("The input file should be \"%s.zip\" with the following structure.\n\n", studentID) +
                String.format("%s.zip \n", studentID) +
                "|- src \n" +
                "|- META-INF \n" +
                "|  |- MANIFEST.MF \n";
    }
}
