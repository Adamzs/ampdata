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

import net.java.dev.designgridlayout.DesignGridLayout;

/**
 * The Class DropDatabasePanel.
 */
@SuppressWarnings("serial")
public class DropDatabasePanel extends JPanel {
    public JComboBox<String> databaseSelector = new JComboBox<String>();

    /**
     * Instantiates a new drop database panel.
     *
     * @param databases the databases
     */
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
