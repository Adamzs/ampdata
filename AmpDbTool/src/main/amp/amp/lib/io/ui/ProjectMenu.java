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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * The Class ProjectMenu.
 */
@SuppressWarnings("serial")
public class ProjectMenu extends JMenu implements ActionListener {
    public static final String QUIT = "QUIT_PROJECT";
    public static final String CLOSE = "CLOSE_PROJECT";
    public static final String OPEN = "OPEN_PROJECT";
    public static final String SAVE = "SAVE_PROJECT";
    private JMenuItem openItem = new JMenuItem("Open Project...");
    private JMenuItem closeItem = new JMenuItem("Close Project...");
    private JMenuItem saveItem = new JMenuItem("Save Output...");
    private JMenuItem quitItem = new JMenuItem("Quit");
    private JMenuItem[] items = { openItem, closeItem, saveItem, quitItem };

    /**
     * Instantiates a new project menu.
     */
    public ProjectMenu() {
        this.setText("Project");
        for (JMenuItem item : items) {
            this.add(item);
            item.setEnabled(true);
            item.addActionListener(this);
        }
        closeItem.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openItem) {
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OPEN));
            closeItem.setEnabled(true);
        } else if (e.getSource() == closeItem) {
            closeItem.setEnabled(false);
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, CLOSE));
        } else if (e.getSource() == saveItem) {
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, SAVE));
        } else if (e.getSource() == quitItem) {
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, QUIT));
        }

    }

}
