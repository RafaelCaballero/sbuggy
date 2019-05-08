package view;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

public class MenuTree extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int numopt = 5;
	JMenuItem items[] = new JMenuItem[numopt];

	public MenuTree(String label) {
		super(label);
		items[0] = new JMenuItem("Valid");
		add(items[0]);

		items[1] = new JMenuItem("Invalid");
		add(items[1]);
		items[1].setEnabled(true);

		items[2] = new JMenuItem("Unknown");
		add(items[2]);
		items[2].setEnabled(true);

		addSeparator();
		items[3] = new JMenuItem("Trusted");
		add(items[3]);
		items[3].setEnabled(true);

		addSeparator();
		items[4] = new JMenuItem("Filter & Order");
		add(items[4]);
		items[4].setEnabled(true);

		setBorder(new BevelBorder(BevelBorder.RAISED));

	}

	/**
	 * Adds an action listener to each menu option
	 * 
	 * @param act
	 */
	public void setActionListener(ActionListener act) {
		for (int i = 0; i < numopt; i++) {
			items[i].addActionListener(act);
		}

	}

	public void disableConnect() {
		items[0].setText("Disconnect");

	}

}
