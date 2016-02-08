package tw.edu.ncku.csie.selab.jojs;

import net.lingala.zip4j.core.ZipFile;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Judger {
    private static final File TESTCASE_FOLDER = new File(JOJS.getConfig("execution").getString("testcase_folder"));
    private static final int TIMEOUT = JOJS.getConfig("execution").getInt("timeout");
    private String hwID, studentID;
    private File workingDirectory;
    private File srcFolder, binFolder;

    public Judger(String hwID, String studentID) throws IOException {
        if (!studentID.matches("[A-Z][0-9]{8}"))
            throw new IllegalArgumentException("The student ID is invalid: " + studentID);

        this.hwID = hwID;
        this.studentID = studentID;

        // Create working directorty
        workingDirectory = new File(FileUtils.getTempDirectory(), hwID+"/"+studentID);
        srcFolder = new File(workingDirectory, studentID);
        binFolder = new File(workingDirectory, "bin");

        FileUtils.deleteDirectory(workingDirectory);
        FileUtils.forceMkdir(srcFolder);
        FileUtils.forceMkdir(binFolder);
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void compile(File zipFile) throws Exception {
        // Unzip source code
        if (!zipFile.getName().endsWith(String.format("%s.zip", studentID)))
            throw new Exception("The zip file name should be \"YOUR_STUDENT_ID.zip\"");
        new ZipFile(zipFile).extractAll(workingDirectory.getAbsolutePath());
        if (!srcFolder.exists())
            throw new Exception("There must be a folder named \"YOUR_STUDENT_ID\" in the zip file.");
        // Compile
        new CompilationTask(srcFolder, binFolder).run();
    }

    public JudgeResult execute(boolean stdin) throws Exception {
        // Get main class (entry point) to execute
        File manifest = new File(workingDirectory, "META-INF/MANIFEST.MF");
        if (!manifest.exists())
            throw new Exception("There should be a META-INF/MANIFEST.MF file in the zip file.");
        String entryPoint = null;
        for (String line : FileUtils.readFileToString(manifest).replace("\r", "").split("\n"))
            if (line.matches("(Main-Class:)\\s*.+"))
                entryPoint = line.replace("Main-Class:", "").trim();
        if (entryPoint == null)
            throw new Exception("The Main-Class is not specified.");

        // Execute the program with time limit
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<JudgeResult> future = executor.submit(new ExecutionTask(new File(TESTCASE_FOLDER, hwID+".json"), binFolder, entryPoint, stdin));
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new Exception(String.format("Your program exceeded time limit %d seconds.", TIMEOUT), e);
        } finally {
            executor.shutdownNow();
        }
    }
}
