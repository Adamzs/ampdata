package amp.lib.io.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;

@SuppressWarnings("serial")
public class CreateDatabasePanel extends JPanel {
    private JTextField nameField = new JTextField();
    private JTextField passwordField = new JTextField();
    private JTextField userField = new JTextField();

    public CreateDatabasePanel(String dbName, String dbUser, String dbPassword) {
        if (dbName != null)
            nameField.setText(dbName);
        if (dbUser != null)
            userField.setText(dbUser);
        if (dbPassword != null)
            passwordField.setText(dbPassword);
        DesignGridLayout dgl = new DesignGridLayout(this);
        dgl.row().grid(new JLabel("Database")).add(nameField);
        dgl.row().grid(new JLabel("User")).add(userField);
        dgl.row().grid(new JLabel("Password")).add(passwordField);
    }

    public String getDatabaseName() {
        return nameField.getText();
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public String getUser() {
        return userField.getText();
    }

}
