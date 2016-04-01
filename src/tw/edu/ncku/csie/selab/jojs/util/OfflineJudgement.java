package tw.edu.ncku.csie.selab.jojs.util;

import org.apache.commons.io.FileUtils;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;

import tw.edu.ncku.csie.selab.jojs.ExecutionResult;
import tw.edu.ncku.csie.selab.jojs.JOJS;
import tw.edu.ncku.csie.selab.jojs.JudgeException;
import tw.edu.ncku.csie.selab.jojs.JudgeResult;
import tw.edu.ncku.csie.selab.jojs.judger.Judger;
import tw.edu.ncku.csie.selab.jojs.judger.OfflineJudger;

public class OfflineJudgement extends JFrame {

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        EventQueue.invokeLater(() -> {
            try {
                OfflineJudgement frame = new OfflineJudgement();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }



    private JTextField hwID, base;
    private JFileChooser fileChooser;
    private JComboBox<String> mode;
    private JCheckBox moss;
    private JButton btn_run, btn_save;
    private JProgressBar progressBar;

    private StringBuilder summary;
    private String mossID = "";


	public OfflineJudgement() throws UnsupportedEncodingException {
		setResizable(false);
		setTitle("JOJS Offline Version");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 415, 395);
        setLocationRelativeTo(null);
        JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

        // Settings
		JPanel panel_setting = new JPanel();
		panel_setting.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_setting.setBounds(5, 5, 193, 100);
		contentPane.add(panel_setting);
		panel_setting.setLayout(null);
		
		JLabel label1 = new JLabel("hwID:");
		label1.setBounds(5, 8, 50, 19);
		panel_setting.add(label1);
		label1.setFont(new Font("Monospaced", Font.PLAIN, 12));
		
		hwID = new JTextField();
		hwID.setBounds(55, 5, 50, 25);
		panel_setting.add(hwID);
		hwID.setFont(new Font("Monospaced", Font.PLAIN, 12));
		hwID.setColumns(5);
		
		mode = new JComboBox<>();
		mode.setBounds(119, 5, 60, 25);
		panel_setting.add(mode);
		mode.setFont(new Font("Monospaced", Font.PLAIN, 12));
		mode.setModel(new DefaultComboBoxModel<>(new String[] {"args", "stdin"}));
		mode.setSelectedIndex(0);

		JLabel label2 = new JLabel("base: ");
		label2.setBounds(5, 39, 50, 19);
		panel_setting.add(label2);
		label2.setFont(new Font("Monospaced", Font.PLAIN, 12));
		
		base = new JTextField();
		base.setBounds(55, 36, 50, 25);
		panel_setting.add(base);
		base.setText("20");
		base.setFont(new Font("Monospaced", Font.PLAIN, 12));
		base.setColumns(5);
		
		moss = new JCheckBox("Moss");
		moss.setBounds(5, 64, 174, 27);
        moss.setFont(new Font("Monospaced", Font.PLAIN, 12));
        moss.addActionListener(this::setMossID);
		panel_setting.add(moss);

        // Buttons
		JPanel panel_button = new JPanel();
		panel_button.setBounds(208, 20, 197, 34);
		contentPane.add(panel_button);
		panel_button.setLayout(new GridLayout(1, 2, 30, 0));
		
		btn_run = new JButton("Run");
        btn_run.addActionListener(this::run);
		panel_button.add(btn_run);

		btn_save = new JButton("Save");
        btn_save.addActionListener(this::save);
		panel_button.add(btn_save);

        // Log
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(5, 115, 400, 240);
		contentPane.add(scrollPane);

        JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
        ((DefaultCaret)textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPane.setViewportView(textArea);

        // Redirect stdout to JTextArea
        PrintStream out = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                textArea.append(String.valueOf((char)b));
            }
        }, true, "UTF-8");
        System.setOut(out);
        System.setErr(out);

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setForeground(new Color(50, 205, 50));
		progressBar.setBounds(208, 85, 197, 20);
        progressBar.setMinimum(0);
        progressBar.setStringPainted(true);
		contentPane.add(progressBar);

        // File chooser
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

    private void setMossID(ActionEvent event) {
        if (moss.isSelected()) {
            String inputValue = JOptionPane.showInputDialog("Please input your MOSS ID", mossID);
            if (inputValue != null && inputValue.length() > 0) {
                mossID = inputValue;
                moss.setText(String.format("Moss (%s)", mossID));
            } else {
                moss.setSelected(false);
                moss.setText("Moss");
            }
        }
    }

    private void run(ActionEvent event) {
        if (hwID.getText().length() * base.getText().length() == 0) {
            JOptionPane.showMessageDialog(null, "Please fill the hwID and base score");
            return;
        }
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            btn_run.setEnabled(false);
            btn_save.setEnabled(false);
            new Thread(() -> {
                run(fileChooser.getSelectedFile());
            }).start();
        }
    }

    private void run(File folder) {
        List<File> files = new ArrayList<>(FileUtils.listFiles(folder, new String[] {"zip"}, true));
        List<File> validFiles = new ArrayList<>(); // For moss
        int total = files.size();
        int progress = 0;

        try {
            if (total == 0) {
                JOptionPane.showMessageDialog(null, "No zip file found");
                return;
            }

            String hwID = this.hwID.getText();
            Judger.Mode mode = (this.mode.getSelectedIndex() == 0) ? Judger.Mode.STANDARD : Judger.Mode.STDIN;
            int base;
            try {
                base = Integer.parseInt(this.base.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "The base field is not a valid number");
                return;
            }

            summary = new StringBuilder("id,score,comment\n");
            progressBar.setValue(0);
            progressBar.setMaximum(total);

            for (File file : files) {
                progressBar.setValue(progress);
                String studentID = file.getName().replace(".zip", "");
                System.out.println(String.format("========== [%s] (%d/%d) ==========", studentID, ++progress, total));

                int score = 0;
                StringBuilder comment = new StringBuilder();
                try {
                    Future<JudgeResult> future = JOJS.judge(new OfflineJudger(hwID, studentID, (p,msg)->{}, file), mode);
                    JudgeResult judgeResult = future.get();
                    score = judgeResult.getScore(base);
                    ExecutionResult[] executionResults = judgeResult.getResults();
                    for (int j=0; j<executionResults.length; j++)
                        if (!executionResults[j].isPassed())
                            comment.append(j+1).append("; ");
                    validFiles.add(file);
                } catch (Exception e) {
                    if (e.getCause() instanceof Exception)
                        e = (Exception) e.getCause();
                    if (e instanceof JudgeException) {
                        JudgeException ex = (JudgeException) e;
                        JudgeException.ErrorCode errorCode = ex.getErrorCode();
                        if (errorCode==JudgeException.ErrorCode.INVALID_STUDENT_ID) {
                            System.out.println(errorCode+"\n");
                            continue;
                        }
                        comment.append(errorCode);
                    } else {
                        e.printStackTrace();
                    }
                }
                summary.append(studentID).append(",").append(score).append(",").append(comment).append("\n");
                System.out.println(String.format("%s => %d %s \n", studentID, score, (comment.length()>0) ? "/ "+comment : ""));
            }

            // Apply plagiarism detection
            if (moss.isSelected()) {
                PlagiarismDetector detector = new PlagiarismDetector(mossID);
                System.out.println("Results are available at " + detector.start(validFiles));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            btn_run.setEnabled(true);
            btn_save.setEnabled(true);
        }

        progressBar.setValue(total);
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, "Done");
    }

    private void save(ActionEvent event) {
        if (summary != null && summary.length() > 0) {
            JFileChooser chooser = new JFileChooser(fileChooser.getCurrentDirectory());
            chooser.setSelectedFile(new File(String.format("summary_%s.csv", hwID.getText())));
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    File output = chooser.getSelectedFile();
                    if (output.exists()) {
                        int option = JOptionPane.showOptionDialog(null,
                                "The file already exists. Do you want to overwrite it?", "File already exists",
                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, null, null);
                        switch (option) {
                            case JOptionPane.YES_OPTION:
                                FileUtils.forceDelete(output); break;
                            case JOptionPane.NO_OPTION:
                                save(event); return;
                            default:
                                return;
                        }
                    }
                    FileUtils.writeStringToFile(output, summary.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "No summary available");
        }
    }
}
