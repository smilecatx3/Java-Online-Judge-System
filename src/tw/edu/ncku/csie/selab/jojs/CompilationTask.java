package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import tw.edu.ncku.csie.selab.jojs.util.Executor;

public class CompilationTask {
    private static String ANT;
    private static String BUILD_FILE_TEMPLATE;
    private File srcFolder, binFolder;

    static {
        try {
            File ant = new File(JOJS.CONFIG.getString("ant_home"), "lib/ant-launcher.jar");
            if (!ant.exists())
                throw new FileNotFoundException(String.format("Cannot find the file \"%s\"", ant.getAbsolutePath()));
            ANT = ant.getAbsolutePath();
            BUILD_FILE_TEMPLATE = IOUtils.toString(JOJS.class.getResourceAsStream("/data/build.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompilationTask(File srcFolder, File binFolder) throws IOException {
        this.srcFolder = srcFolder;
        this.binFolder = binFolder;
    }

    public void execute() throws IOException, JudgeException {
        String[] command = {JOJS.JAVA, "-Dfile.encoding=UTF-8", "-Duser.language=en", "-cp", ANT, "org.apache.tools.ant.launch.Launcher", "-buildfile", generateBuildFile()};
        String output = Executor.execute(command, null);
        if (output.contains("BUILD FAILED")) {
            if (output.contains("unmappable character for encoding UTF-8"))
                throw new JudgeException("Please use UTF-8 encoding for your source files", JudgeException.ErrorCode.UNSUPPORTED_ENCODING);
            else
                throw new JudgeException(getErrorMessage(output), JudgeException.ErrorCode.COMPILE_ERROR);
        }
    }

    private String generateBuildFile() throws IOException {
        // Build a list of folders as the source to compile
        Collection<File> files = FileUtils.listFilesAndDirs(srcFolder, DirectoryFileFilter.INSTANCE, null);
        StringBuilder srcDir = new StringBuilder();
        for (File file : files)
            srcDir.append(file).append(";");
        // Generate a temp build file
        File buildFile = new File(srcFolder.getParentFile(), String.format("build%d.xml", System.currentTimeMillis()));
        buildFile.deleteOnExit();
        FileUtils.writeStringToFile(buildFile, BUILD_FILE_TEMPLATE.replace("SRCDIR", srcDir).replace("DESTDIR", binFolder.getAbsolutePath()));
        return buildFile.getAbsolutePath();
    }

    private String getErrorMessage(String output) {
        String srcPath = srcFolder.getAbsolutePath() + File.separator;
        String binPath = binFolder.getAbsolutePath();
        if (output.contains(binPath)) {
            StringBuilder result = new StringBuilder();
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (!line.contains("[javac]") || line.contains(binPath))
                    continue;
                if (line.contains(srcPath))
                    line = line.replace(srcPath, "").replace("\\", "/");
                result.append(line.trim().replace("[javac] ", "")).append("\n");
            }
            return result.toString();
        } else {
            return output;
        }
    }
}
