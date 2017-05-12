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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The Class OpenlogPanel.
 */
@SuppressWarnings("serial")
public class CreateLogPanel extends JPanel implements ActionListener {
    public JTextField logPathField = new JTextField(50);
    private JCheckBox errorFilterCheckBox = new JCheckBox("Filter Error Messages");
    private JButton selectButton = new JButton("Select...");

    /**
     * Instantiates a new open log panel.
     * @param logPath the log path
     */
    public CreateLogPanel(String logPath, boolean filterErrors) {
        selectButton.addActionListener(this);
        errorFilterCheckBox.setSelected(filterErrors);

        this.setLayout(new FlowLayout());
        this.add(new JLabel("Log File"));
        this.add(logPathField);
        this.add(selectButton);
        this.add(errorFilterCheckBox, filterErrors);
        if (logPath != null) {
            logPathField.setText(logPath);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectButton) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            chooser.setSize(1000, 1000);
            chooser.setLocation(100, 100);
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Log Files", "log"));
            chooser.showOpenDialog(null);
            if (chooser.getSelectedFile() != null) {
                logPathField.setText(chooser.getSelectedFile().getPath());
            }
        }

    }

    public boolean getFilterErrors() {
        return errorFilterCheckBox.isSelected();
    }

    public String getlogPath() {
        return logPathField.getText();
    }

    public void setFilterErrors(boolean filterErrors) {
        errorFilterCheckBox.setSelected(filterErrors);
    }

}
