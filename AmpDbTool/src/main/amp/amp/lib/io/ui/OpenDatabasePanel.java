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

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;

/**
 * The Class OpenDatabasePanel.
 */
@SuppressWarnings("serial")
public class OpenDatabasePanel extends JPanel {
    public JComboBox<String> databaseSelector = new JComboBox<String>();
    public JTextField passwordField = new JTextField(15);
    public JTextField userField = new JTextField(15);

    /**
     * Instantiates a new open database panel.
     *
     * @param databases the databases
     * @param dbName the db name
     * @param dbUser the db user
     * @param dbPassword the db password
     */
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
