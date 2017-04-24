package amp.lib.io.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class FileMenu extends JMenu implements ActionListener {
    public static final String QUIT = "QUIT_FILE";
    public static final String CLOSE = "CLOSE_FILE";
    public static final String OPEN = "OPEN_FILE";
    private JMenuItem openItem = new JMenuItem("Open Project...");
    private JMenuItem closeItem = new JMenuItem("Close Project...");
    private JMenuItem quitItem = new JMenuItem("Quit");
    private JMenuItem[] items = {
                    openItem, closeItem, quitItem
    };

    public FileMenu() {
        this.setText("File");
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
        } else if (e.getSource() == quitItem) {
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, QUIT));
        }

    }

}
