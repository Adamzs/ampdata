package amp.lib.io.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class DatabaseMenu extends JMenu implements ActionListener {
    public static final String OPEN = "OPEN_DB";
    public static final String POPULATE = "POPULATE_DB";
    public static final String CREATE = "CREATE_DB";
    public static final String SCHEMA = "SCHEMA_DB";
    public static final String DROP = "DROP_DB";
    private JMenuItem openItem = new JMenuItem("Open Database...");
    private JMenuItem createItem = new JMenuItem("Create Database...");
    private JMenuItem dropItem = new JMenuItem("Drop Database...");
    private JMenuItem schemaItem = new JMenuItem("Build Database Schema...");
    private JMenuItem populateItem = new JMenuItem("Populate Database...");
    private JMenuItem[] items = {
                    openItem, createItem, dropItem, schemaItem, populateItem
    };
    private String databaseName;

    public DatabaseMenu() {
        this.setText("Database");
        for (JMenuItem item : items) {
            this.add(item);
            item.setEnabled(false);
            item.addActionListener(this);
        }
        openItem.setEnabled(true);
        createItem.setEnabled(true);
        dropItem.setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openItem) {
            // Select databases here
            schemaItem.setEnabled(true);
            populateItem.setEnabled(true);
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OPEN));
        }
        if (e.getSource() == createItem) {
            // create databases here
            schemaItem.setEnabled(true);
            populateItem.setEnabled(true);
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, CREATE));
        }
        if (e.getSource() == schemaItem) {
            // create databases here
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, SCHEMA));
        } else if (e.getSource() == populateItem) {
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, POPULATE));
        }
        if (e.getSource() == dropItem) {
            schemaItem.setEnabled(false);
            populateItem.setEnabled(false);
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, DROP));
        }

    }

    public String getDatabaseName() {
        return databaseName;
    }

}
