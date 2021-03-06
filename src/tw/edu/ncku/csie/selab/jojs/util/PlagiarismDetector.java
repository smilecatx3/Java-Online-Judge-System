package tw.edu.ncku.csie.selab.jojs.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.zielke.moji.MossException;
import it.zielke.moji.SocketClient;

public class PlagiarismDetector {
    private SocketClient socketClient;

    public PlagiarismDetector(String mossUserID) throws MossException, IOException {
        socketClient = new SocketClient();
        socketClient.setUserID(mossUserID);
        socketClient.setLanguage("java");
        socketClient.run();
    }

    public String start(String files) throws MossException, ZipException, IOException {
        List<File> list = new ArrayList<>();
        for (String file : files.substring(1, files.length()-1).split(","))
            list.add(new File(file.trim()));
        return start(list);
    }

    public String start(List<File> files) throws IOException, ZipException, MossException {
        File tempDir = new File(FileUtils.getTempDirectory(), "moss");
        System.out.println("[MOSS] Start");
        System.out.println("[MOSS] Working directory: " + tempDir);
        System.out.println("[MOSS] Input files: " + files);

        if (tempDir.exists())
            FileUtils.forceDelete(tempDir);
        for (File file : files) {
            String studentID = file.getName().replace(".zip", "");
            File extractPath = new File(tempDir, "zip/"+studentID);
            new ZipFile(file).extractAll(extractPath.getAbsolutePath());

            File src = new File(extractPath, "src");
            File dst = new File(tempDir, "submit/"+studentID);
            FileUtils.moveDirectory(src, dst);

            upload(dst);
        }
        socketClient.sendQuery();
        return socketClient.getResultURL().toString();
    }

    private void upload(File folder) throws IOException {
        // Need remove all non ascii words and comments to prevent exceptions thrown by MOJI
        String regex = "[" +
                "\\p{InCJK_UNIFIED_IDEOGRAPHS}" +
                "\\p{InBOPOMOFO}" +
                "\\p{InCJK_SYMBOLS_AND_PUNCTUATION}" +
                "\\p{InHIRAGANA}" + "\\p{InKATAKANA}" +
                "\\p{InHANGUL_SYLLABLES}" +
                "]";
        for (File input : FileUtils.listFiles(folder, new String[]{"java"}, true)) {
            FileUtils.writeStringToFile(input,
                    FileUtils.readFileToString(input)
                            .replaceAll("//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/", "$1") // Remove comments
                            .replaceAll(regex, "") // Remove non ascii words
                            .trim());
            socketClient.uploadFile(input);
        }
    }
}
