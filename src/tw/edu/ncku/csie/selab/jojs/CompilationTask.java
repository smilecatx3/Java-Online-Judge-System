package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class CompilationTask {
    private static final String ANT_PATH = JOJS.getConfig("compilation").getString("ant_path");
    private static String BUILD_FILE_TEMPLATE = null;
    private File srcFolder, binFolder;

    static {
        try {
            BUILD_FILE_TEMPLATE = IOUtils.toString(JOJS.class.getResourceAsStream("/data/build.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompilationTask(File srcFolder, File binFolder) throws IOException {
        this.srcFolder = srcFolder;
        this.binFolder = binFolder;
    }

    public void run() throws Exception {
        String[] command = {ANT_PATH, "-buildfile", generateBuildFile()};
        String output = TaskExecutor.execute(command);
        if (output.contains("BUILD FAILED"))
            throw new Exception("ERROR: Compilation failed. \n\n" + getErrorMessage(output));
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
        return output.substring(output.indexOf(binFolder.getAbsolutePath())+ binFolder.getAbsolutePath().length(), output.lastIndexOf("BUILD FAILED"))
                .replace("[javac] ", "").replace(srcFolder.getAbsolutePath()+File.separator, "");
    }
}
