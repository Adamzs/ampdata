package amp.lib.io.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class OpenProjectPanel extends JPanel implements ActionListener {
    public JTextField projectPathField = new JTextField(50);
    private JButton selectButton = new JButton("Select...");

    public OpenProjectPanel(String projectPath) {
        // selectButton.setOpaque(true);
        // selectButton.setBackground(Color.blue);
        selectButton.addActionListener(this);

        this.setLayout(new FlowLayout());
        this.add(new JLabel("Project Root"));
        this.add(projectPathField);
        this.add(selectButton);
        if (projectPath != null) {
            projectPathField.setText(projectPath);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectButton) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            chooser.setSize(1000, 1000);
            chooser.setLocation(100, 100);
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.showOpenDialog(null);
            if (chooser.getSelectedFile() != null) {
                projectPathField.setText(chooser.getSelectedFile().getPath());
            }
        }

    }

    public String getProjectPath() {
        return projectPathField.getText();
    }

}
