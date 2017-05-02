package amp.lib.io.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class OpenSQLPanel extends JPanel implements ActionListener {
    private JTextField sqlPathField = new JTextField(50);
    private JButton selectButton = new JButton("Select...");
    private File sqlRoot;

    List<File> sqlFiles = new ArrayList<>();

    public OpenSQLPanel(String sqlPath) {
        selectButton.addActionListener(this);

        this.setLayout(new FlowLayout());
        this.add(new JLabel("SQL Directories and Files"));
        this.add(sqlPathField);
        this.add(selectButton);
        if (sqlPath == null || sqlPath.trim().isEmpty()) {
            sqlPath = System.getProperty("user.dir");
        }
        sqlRoot = new File(sqlPath);
        if (!sqlRoot.exists()) {
            sqlPath = System.getProperty("user.dir");
        }
        sqlPathField.setText(sqlPath);
        sqlRoot = new File(sqlPath);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectButton) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter sqlFilter = new FileNameExtensionFilter("SQL Scripts", "sql");
            chooser.addChoosableFileFilter(sqlFilter);
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            chooser.setSize(1000, 1000);
            chooser.setLocation(100, 100);
            chooser.setCurrentDirectory(sqlRoot);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.showOpenDialog(null);
            if (chooser.getSelectedFiles() != null) {
                for (File f : chooser.getSelectedFiles()) {
                    if (f.isDirectory()) {
                        for (File f2 : f.listFiles()) {
                            if (f2.getName().endsWith(".sql")) {
                                sqlFiles.add(f2);
                            }
                        }

                    } else if (f.getName().endsWith(".sql")) {
                        sqlFiles.add(f);
                    }
                }
            }
        }

    }

    public List<File> getSqlFiles() {
        return sqlFiles;
    }

}
