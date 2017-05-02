package amp.lib.io.ui;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.java.dev.designgridlayout.DesignGridLayout;

@SuppressWarnings("serial")
public class DropDatabasePanel extends JPanel {
    public JComboBox<String> databaseSelector = new JComboBox<String>();

    public DropDatabasePanel(List<String> databases) {
        for (String db : databases) {
            databaseSelector.addItem(db);
        }
        DesignGridLayout dgl = new DesignGridLayout(this);
        dgl.row().grid(new JLabel("Database")).add(databaseSelector);
    }

    public String getDatabaseName() {
        return (String) databaseSelector.getSelectedItem();
    }

}
