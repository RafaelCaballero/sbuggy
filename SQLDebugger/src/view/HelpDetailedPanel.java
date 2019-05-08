package view;

import java.awt.LayoutManager;


import javax.swing.JEditorPane;
import javax.swing.JPanel;


public class HelpDetailedPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4052740535327147665L;
	private JEditorPane editorPane;

	public HelpDetailedPanel() {
		// TODO Auto-generated constructor stub
	}

	public HelpDetailedPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public HelpDetailedPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public HelpDetailedPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public HelpDetailedPanel(String text) {
		initComponents(text);
	}

	private void initComponents(String text) {
		editorPane = new JEditorPane("text/html",text);
		editorPane.setEditable(false);

		this.add(editorPane);

	}

}
