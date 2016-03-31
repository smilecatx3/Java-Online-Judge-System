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
    private JTextField textField_srcFolder;
    private JFileChooser fileChooser;
    private JButton btn_create;

    private File srcFolder, binFolder;
    private String[] srcFilter = {"java"};
    private String[] binFilter = {"class"};


	private InputCreator() {
        drawComponents();

        // Init file chooser
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        setSrcFolder(fileChooser.getCurrentDirectory());
        setBinFolder(fileChooser.getCurrentDirectory());
	}

    private void drawComponents() {
        setResizable(false);
        setTitle("Input Creator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 400, 150);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // Src components
        JButton btn_srcFolder = new JButton("...");
        btn_srcFolder.setToolTipText("Select src folder");
        btn_srcFolder.setBounds(361, 10, 23, 23);
        btn_srcFolder.addActionListener(this::selectSrcFolder);
        contentPane.add(btn_srcFolder);

        textField_srcFolder = new JTextField();
        textField_srcFolder.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textField_srcFolder.setEditable(false);
        textField_srcFolder.setColumns(10);
        textField_srcFolder.setBackground(Color.WHITE);
        textField_srcFolder.setBounds(10, 10, 341, 21);
        contentPane.add(textField_srcFolder);

        // Bin components
        JButton btn_binFolder = new JButton("...");
        btn_binFolder.setToolTipText("Select bin folder");
        btn_binFolder.setBounds(361, 43, 23, 23);
        btn_binFolder.addActionListener(this::selectBinFolder);

        comboBox_mainClass = new JComboBox<>();
        comboBox_mainClass.setFont(new Font("Monospaced", Font.PLAIN, 12));
        comboBox_mainClass.setBounds(10, 44, 341, 21);
        contentPane.add(comboBox_mainClass);
        contentPane.add(btn_binFolder);

        // Create button
        btn_create = new JButton("CREATE");
        btn_create.setEnabled(false);
        btn_create.setFont(new Font("Monospaced", Font.PLAIN, 16));
        btn_create.setBounds(119, 75, 150, 40);
        btn_create.addActionListener(this::create);
        contentPane.add(btn_create);
    }

    private void setSrcFolder(File root) {
        File srcFolder = new File(root, "src");
        if (validateFolder(srcFolder, srcFilter, false)) {
            this.srcFolder = srcFolder;
            textField_srcFolder.setText(srcFolder.getAbsolutePath());
        }
    }

    private void setBinFolder(File root) {
        // eclipse default
        File binFolder = new File(root, "bin");
        if (validateFolder(binFolder, binFilter, false)) {
            this.binFolder = binFolder;
            setMainClassList();
            return;
        }

        // Intellij IDEA default
        File[] folders = new File(root, "out/production").listFiles();
        if (folders != null && folders.length > 0)
            binFolder = folders[0];
        if (validateFolder(binFolder, binFilter, false)) {
            this.binFolder = binFolder;
            setMainClassList();
        }
    }

    private void selectSrcFolder(ActionEvent event) {
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        srcFolder = fileChooser.getSelectedFile();
        if (validateFolder(srcFolder, srcFilter)) {
            btn_create.setEnabled(false);
            textField_srcFolder.setText(srcFolder.getAbsolutePath());
            comboBox_mainClass.removeAllItems();
            setBinFolder(srcFolder.getParentFile());
        }
    }

    private void selectBinFolder(ActionEvent event) {
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        binFolder = fileChooser.getSelectedFile();
        if (validateFolder(binFolder, binFilter))
            setMainClassList();
    }

    private void setMainClassList() {
        try {
            ClassLoader classLoader = new URLClassLoader(new URL[]{binFolder.toURI().toURL()});
            for (File file : FileUtils.listFiles(binFolder, binFilter, true)) {
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
            FileUtils.copyDirectory(this.srcFolder, srcFolder);

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

    private boolean validateFolder(File folder, String[] filter) {
        return validateFolder(folder, filter, true);
    }

    private boolean validateFolder(File folder, String[] filter, boolean showMessage) {
        assert filter.length > 0;
        if (folder.exists() && folder.isDirectory() && FileUtils.listFiles(folder, filter, true).size() > 0) {
            return true;
        } else {
            if (showMessage)
                JOptionPane.showMessageDialog(null, String.format("The directory should contain .%s files", filter[0]));
            return false;
        }
    }
}
