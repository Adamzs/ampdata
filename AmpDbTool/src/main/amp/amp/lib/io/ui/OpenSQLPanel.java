/***************************************************************************
 *
 * <rrl>
 * =========================================================================
 *                                  LEGEND
 *
 * Use, duplication, or disclosure by the Government is as set forth in the
 * Rights in technical data noncommercial items clause DFAR 252.227-7013 and
 * Rights in noncommercial computer software and noncommercial computer
 * software documentation clause DFAR 252.227-7014, with the exception of
 * third party software known as Sun Microsystems' Java Runtime Environment
 * (JRE), Quest Software's JClass, Oracle's JDBC, and JGoodies which are
 * separately governed under their commercial licenses.  Refer to the
 * license directory for information regarding the open source packages used
 * by this software.
 *
 * Copyright 2016 by BBN Technologies Corporation.
 * =========================================================================
 * </rrl>
 *
 **************************************************************************/
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

/**
 * The Class OpenSQLPanel.
 */
@SuppressWarnings("serial")
public class OpenSQLPanel extends JPanel implements ActionListener {
    private JTextField sqlRootField = new JTextField(50);
    private JButton selectButton = new JButton("Select...");
    private File sqlRootDir;

    List<File> sqlFiles = new ArrayList<>();

    /**
     * Instantiates a new open SQL panel.
     * @param sqlRoot the sql path
     */
    public OpenSQLPanel(String sqlRoot) {
        selectButton.addActionListener(this);

        this.setLayout(new FlowLayout());
        this.add(new JLabel("SQL Directories and Files"));
        this.add(sqlRootField);
        this.add(selectButton);
        if (sqlRoot == null || sqlRoot.trim().isEmpty()) {
            sqlRoot = System.getProperty("user.dir");
        }
        sqlRootDir = new File(sqlRoot);
        if (!sqlRootDir.exists()) {
            sqlRoot = System.getProperty("user.dir");
        }
        sqlRootDir = new File(sqlRoot);
        sqlRootField.setText(sqlRoot);
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
            chooser.setCurrentDirectory(sqlRootDir);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.showOpenDialog(null);
            if (chooser.getSelectedFiles() != null) {
                for (File f : chooser.getSelectedFiles()) {
                    if (f.isDirectory()) {
                        sqlRootField.setText(f.getAbsolutePath());
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

    public String getSqlRoot() {
        return sqlRootField.getText();
    }

}
