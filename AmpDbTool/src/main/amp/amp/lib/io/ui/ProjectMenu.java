package amp.lib.io.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

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
