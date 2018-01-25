package amp.lib.io.ui;

import java.awt.BorderLayout;
import java.awt.List;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.*;

import amp.lib.io.db.Database;

/**
 * The Class RemoveScenarioPanel.
 */
@SuppressWarnings("serial")
public class RemoveScenarioPanel extends JPanel {
	
	JList<String> list;
	
	public RemoveScenarioPanel(String dbName, String dbUser, String dbPassword){
		create();
	}
	
	
	
	public void create(){
		Database database = Database.getDatabase();
		ArrayList<String> scens = new ArrayList<String>();
		try {
			scens = database.getScenarioNames();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(scens.size() == 0){
			this.add(new JLabel("No scenarios in Database"));
		}else{
			String[] scenArr = new String[scens.size()];
			for(int i = 0; i < scenArr.length; i++){
				scenArr[i] = scens.get(i);
			}
			setLayout(new BorderLayout());
			list = new JList<String>(scenArr);
		    list.setSelectionMode(
		        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		    this.add(new JLabel("Select scenario(s) to delete:"), BorderLayout.NORTH);
		    this.add(list, BorderLayout.CENTER);
		}
	}
	
	
	public ArrayList<String> getSelectedScenarios(){
		return (ArrayList<String>) list.getSelectedValuesList();

	}
}
