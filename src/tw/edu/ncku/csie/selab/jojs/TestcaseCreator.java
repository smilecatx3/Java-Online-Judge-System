package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.io.FileUtils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

public class TestcaseCreator {

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
        int inputPerTestcase = Integer.parseInt(args[2]);
        int outputPerTestcase = Integer.parseInt(args[3]);
        System.out.println("Generating testcase ...");
        System.out.println(String.format("Input file: \"%s\" (%d input per testcase)", inputFile, inputPerTestcase));
        System.out.println(String.format("Output file: \"%s\" (%d output per testcase)", outputFile, outputPerTestcase));

        // Create input testcases
        StringBuilder inputBuilder = new StringBuilder();
        String[] linesOfInput = FileUtils.readFileToString(inputFile).replace("\r", "").split("\n");
        for (int i=0; i<linesOfInput.length; ) {
            inputBuilder.append("\t\t[");
            for (int j=0; j<inputPerTestcase; j++)
                inputBuilder.append("\"").append(linesOfInput[i++]).append("\"").append(", ");
            inputBuilder.delete(inputBuilder.lastIndexOf(","), inputBuilder.length());
            inputBuilder.append("], \n");
        }
        inputBuilder.delete(inputBuilder.lastIndexOf(","), inputBuilder.length());

        // Create output testcases
        String[] linesOfOutput = FileUtils.readFileToString(outputFile).replace("\r", "").split("\n");
        StringBuilder outputBuilder = new StringBuilder();
        for (int i=0; i<linesOfOutput.length; ) {
            outputBuilder.append("\t\t[");
            for (int j=0; j<outputPerTestcase; j++)
                outputBuilder.append("\"").append(linesOfOutput[i++]).append("\"").append(", ");
            outputBuilder.delete(outputBuilder.lastIndexOf(","), outputBuilder.length());
            outputBuilder.append("], \n");
        }
        outputBuilder.delete(outputBuilder.lastIndexOf(","), outputBuilder.length());

        // Save file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(inputFile.getParentFile(), "hw.json"));
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            FileUtils.writeStringToFile(
                    fileChooser.getSelectedFile(),
                    String.format("{\n\t\"inputs\" : [\n%s\n\t],\n\n\t\"outputs\" : [\n%s\n\t]\n}", inputBuilder, outputBuilder)
            );
        }
        System.out.println("done");
    }
}
