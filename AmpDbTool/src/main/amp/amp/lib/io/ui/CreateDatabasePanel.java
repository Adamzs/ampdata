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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;

/**
 * The Class CreateDatabasePanel.
 */
@SuppressWarnings("serial")
public class CreateDatabasePanel extends JPanel {
    private JTextField nameField = new JTextField(15);
    private JTextField passwordField = new JTextField(15);
    private JTextField userField = new JTextField(15);

    /**
     * Instantiates a new creates the database panel.
     * @param dbName the db name
     * @param dbUser the db user
     * @param dbPassword the db password
     */
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
