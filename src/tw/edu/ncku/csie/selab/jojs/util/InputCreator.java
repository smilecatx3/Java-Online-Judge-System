package tw.edu.ncku.csie.selab.jojs.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class InputCreator extends JFrame {

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        EventQueue.invokeLater(() -> {
            try {
                InputCreator frame = new InputCreator();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }



    private JComboBox<String> comboBox_mainClass;
    private JTextField textField_projectFolder;
    private JFileChooser fileChooser;
    private JButton btn_create;

    private File projectFolder = new File(System.getProperty("user.dir"));
    private File projectSrcFolder;
    private List<File> classFiles;


	public InputCreator() {
		setResizable(false);
		setTitle("Input Creator");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 150);
        JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(projectFolder);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		textField_projectFolder = new JTextField();
		textField_projectFolder.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textField_projectFolder.setBackground(Color.WHITE);
		textField_projectFolder.setEditable(false);
		textField_projectFolder.setBounds(10, 10, 341, 21);
        textField_projectFolder.setColumns(10);
        if (validateProjectFolder())
            textField_projectFolder.setText(projectFolder.getAbsolutePath());
		contentPane.add(textField_projectFolder);

        comboBox_mainClass = new JComboBox<>();
        comboBox_mainClass.setFont(new Font("Monospaced", Font.PLAIN, 12));
        comboBox_mainClass.setBounds(10, 41, 341, 21);
        contentPane.add(comboBox_mainClass);

        JButton btn_chooseProjectFolder = new JButton("...");
		btn_chooseProjectFolder.setBounds(361, 9, 23, 23);
        btn_chooseProjectFolder.addActionListener(this::chooseProjectFolder);
		contentPane.add(btn_chooseProjectFolder);

        JButton btn_chooseBinFolder = new JButton("...");
        btn_chooseBinFolder.setBounds(361, 42, 23, 23);
        btn_chooseBinFolder.addActionListener(this::chooseBinFolder);
        contentPane.add(btn_chooseBinFolder);

        btn_create = new JButton("CREATE");
        btn_create.setEnabled(false);
        btn_create.setFont(new Font("Monospaced", Font.PLAIN, 16));
        btn_create.setBounds(120, 72, 150, 40);
        btn_create.addActionListener(this::create);
        contentPane.add(btn_create);
	}

    private boolean validateProjectFolder() {
        // TODO exception will occur if project root folder contains java file
        List<File> srcFiles = new ArrayList<>(FileUtils.listFiles(projectFolder, new String[] {"java"}, true));
        classFiles = new ArrayList<>(FileUtils.listFiles(projectFolder, new String[] {"class"}, true));
        if (srcFiles.size() * classFiles.size() == 0) {
            JOptionPane.showMessageDialog(null, "Your project directory should contain both source files (*.java) and class files (*.class)");
            return false;
        } else {
            String srcRoot = srcFiles.get(0).getAbsolutePath()
                    .substring(projectFolder.getAbsolutePath().length())
                    .replace(File.separator, " ").trim();
            srcRoot = srcRoot.substring(0, srcRoot.indexOf(" "));
            projectSrcFolder = new File(projectFolder, srcRoot);
            return true;
        }
    }

    private void chooseProjectFolder(ActionEvent event) {
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        projectFolder = fileChooser.getSelectedFile();
        if (validateProjectFolder()) {
            textField_projectFolder.setText(projectFolder.getAbsolutePath());
            comboBox_mainClass.removeAllItems();
            btn_create.setEnabled(false);
        }
    }

    private void chooseBinFolder(ActionEvent event) {
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        // Set main class list
        try {
            File binFolder = fileChooser.getSelectedFile();
            ClassLoader classLoader = new URLClassLoader(new URL[]{binFolder.toURI().toURL()});
            for (File file : classFiles) {
                String className = file.getAbsolutePath()
                        .replace(binFolder.getAbsolutePath(), "")
                        .replace(File.separator, ".")
                        .replace(".class", "");
                try {
                    Method method = classLoader
                            .loadClass(className.startsWith(".") ? className.substring(1) : className)
                            .getDeclaredMethod("main", String[].class);
                    comboBox_mainClass.addItem(method.getDeclaringClass().getName());
                } catch (NoClassDefFoundError e) {
                    JOptionPane.showMessageDialog(null, "Please choose the ROOT directory of your class files");
                    break;
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    // Do nothing
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (comboBox_mainClass.getItemCount() > 0)
            btn_create.setEnabled(true);
    }

    private void create(ActionEvent event) {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(projectFolder);
            if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
                return;
            File output = fileChooser.getSelectedFile();
            if (!output.getName().endsWith(".zip"))
                output = new File(output.getParentFile(), output.getName()+".zip");
            if (output.exists()) {
                int option = JOptionPane.showOptionDialog(null, "The file existed. It will be overwritten.", "File Existed",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null, null, null);
                if (option == JOptionPane.OK_OPTION)
                    FileUtils.forceDelete(output);
                else
                    return;
            }

            File tempDir = new File(FileUtils.getTempDirectory(), String.valueOf(System.currentTimeMillis()));
            File manifest = new File(tempDir, "META-INF/MANIFEST.MF");
            File srcFolder = new File(tempDir, "src");

            FileUtils.writeStringToFile(manifest,
                    "Manifest-Version: 1.0\n" +
                    String.format("Main-Class: %s\n", comboBox_mainClass.getSelectedItem()));
            FileUtils.copyDirectory(projectSrcFolder, srcFolder);

            ZipFile zip = new ZipFile(output);
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            zip.addFolder(srcFolder, parameters);
            zip.addFolder(manifest.getParentFile(), parameters);

            FileUtils.forceDelete(tempDir);
            JOptionPane.showMessageDialog(null, String.format("<html>File saved to <b>\"%s\"</b></html>", output), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | ZipException e) {
            JOptionPane.showMessageDialog(null, ExceptionUtils.getStackFrames(e), "ERROR", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

}
