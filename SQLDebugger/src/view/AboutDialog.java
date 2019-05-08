package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class AboutDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AboutDialog(JFrame parent) {
		super(parent, "SBuggy - About", true);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder((Border) new EmptyBorder(new Insets(15, 15, 15, 15)));

		panel.add(new ImageHopla(), BorderLayout.EAST);
		JPanel center = new JPanel(new GridLayout(4, 1));
		Font fontappname = new Font("SansSerif", Font.ITALIC + Font.BOLD, 15);
		JLabel label = new JLabel("SBuggy - Java SQL Debugger ");
		label.setFont(fontappname);
			
		center.add(label);
		center.add(new JLabel("Version 1.2"));
		center.add(new JLabel("Hopla Software - 2015-2016"));
		panel.add(center, BorderLayout.CENTER);

		JPanel p2 = new JPanel();
		JButton ok = new JButton("Ok");
		p2.add(ok);
		panel.add(p2, BorderLayout.SOUTH);
		getContentPane().add(panel);

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setVisible(false);
			}
		});

		setSize(500, 170);
	}
}
