package amp.lib.io.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * The Class ScenarioMenu.
 */
@SuppressWarnings("serial")
public class ScenarioMenu extends JMenu implements ActionListener {
	public static final String REMOVE = "REMOVE_SCEN";
	
	private JMenuItem removeItem = new JMenuItem("Remove Scenarios");
	private JMenuItem[] items = { removeItem};

	
	 /**
     * Instantiates a new scenario menu.
     */
    public ScenarioMenu() {
        this.setText("Scenario");
        for (JMenuItem item : items) {
            this.add(item);
            item.setEnabled(false);
            item.addActionListener(this);
        }
        removeItem.setEnabled(true);
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == removeItem) {
            this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, REMOVE));
        } 
    }
}
