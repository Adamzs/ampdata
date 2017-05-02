package amp.lib.io.ui;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;

@SuppressWarnings("serial")
public class OpenDatabasePanel extends JPanel {
    public JComboBox<String> databaseSelector = new JComboBox<String>();
    public JTextField passwordField = new JTextField();
    public JTextField userField = new JTextField();

    public OpenDatabasePanel(List<String> databases, String dbName, String dbUser, String dbPassword) {
        for (String db : databases) {
            databaseSelector.addItem(db);
        }
        if (dbName != null)
            databaseSelector.setSelectedItem(dbName);
        if (dbUser != null)
            userField.setText(dbUser);
        if (dbPassword != null)
            passwordField.setText(dbPassword);
        DesignGridLayout dgl = new DesignGridLayout(this);
        dgl.row().grid(new JLabel("Database")).add(databaseSelector);
        dgl.row().grid(new JLabel("User")).add(userField);
        dgl.row().grid(new JLabel("Password")).add(passwordField);
    }

    public String getDatabaseName() {
        return (String) databaseSelector.getSelectedItem();
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public String getUser() {
        return userField.getText();
    }

}
