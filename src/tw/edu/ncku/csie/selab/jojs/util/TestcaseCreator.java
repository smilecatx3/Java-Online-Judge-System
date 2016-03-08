package tw.edu.ncku.csie.selab.jojs.util;

import org.apache.commons.io.FileUtils;
import org.json.JSONStringer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

public class TestcaseCreator {
    private List<List<String>> inputs = new ArrayList<>();
    private List<String> outputs = new ArrayList<>();

    private String create(File file) throws Exception {
        String[] lines = FileUtils.readFileToString(file).replace("\r", "").split("\n");
        System.out.println("Generating testcase ...");

        for (int i=0; i<lines.length; i++) {
            String line = lines[i];
            if (line.trim().equals("$INPUT.BEGIN")) {
                StringBuilder testcase = new StringBuilder(line.trim()).append("\n");
                do {
                    line = lines[++i];
                    testcase.append(line).append("\n");
                } while (!line.trim().equals("$OUTPUT.END"));
                parse(testcase.toString());
            }
        }

        JSONStringer json = new JSONStringer();
        json.object();
        // Create inputs entries
        json.key("inputs").array();
        for (List<String> inputList : inputs) {
            json.array();
            inputList.forEach(json::value);
            json.endArray();
        }
        json.endArray();
        // Create outputs entries
        json.key("outputs").array();
        outputs.forEach(json::value);
        json.endArray();

        return json.endObject().toString();
    }

    private void parse(String testcase) throws Exception {
        Pattern pattern = Pattern.compile("\\$INPUT\\.BEGIN(.*)\\$INPUT\\.END.*\\$OUTPUT\\.BEGIN(.*)\\$OUTPUT\\.END.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(testcase);
        if (matcher.matches()) {
            outputs.add(matcher.group(2).trim());
            // Parse inputs
            List<String> inputList = new ArrayList<>();
            String input = matcher.group(1);
            while (input.contains("$BEGIN")) {
                try {
                    int begin = input.indexOf("$BEGIN")+"$BEGIN".length();
                    int end = input.indexOf("$END");
                    inputList.add(input.substring(begin, end).trim());
                    input = input.substring(end+"$END".length());
                } catch (IndexOutOfBoundsException e) {
                    throw new Exception("File format is incorrect", e);
                }
            }
            inputs.add(inputList);
        } else {
            throw new Exception("File format is incorrect");
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        File input = fileChooser.getSelectedFile();
        TestcaseCreator creator = new TestcaseCreator();
        String json = creator.create(input);

        fileChooser.setSelectedFile(new File(input.getParent(), input.getName().replace(".txt", ".json")));
        if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        File output = fileChooser.getSelectedFile();
        if (!output.getName().endsWith(".json"))
            output = new File(output.getParent(), output.getName()+".json");
        FileUtils.writeStringToFile(output, json);
    }
}
