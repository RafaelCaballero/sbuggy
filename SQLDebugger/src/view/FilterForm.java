package view;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Login.java
 *
 * Created on Sep 25, 2012, 4:25:02 AM
 */

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import control.Controller;

/**
 * Displays the filter dialog
 * 
 * @author Rafael Caballero
 */
public class FilterForm extends javax.swing.JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Creates new form Login */
	public FilterForm(Dialog d, boolean b) {
		super(d, b);
		initComponents();
		pack();
		this.setSize(new Dimension(640, 158));

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 *
	 */
	private void initComponents() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		this.setLayout(new BorderLayout());
		mainPanel.setBorder((Border) new EmptyBorder(new Insets(5, 5, 5, 5)));
		JPanel mainCenterPanel = new JPanel(new BorderLayout());
		mainCenterPanel.setBorder((Border) new EmptyBorder(new Insets(15, 15, 15, 15)));
		JPanel southPanel = new JPanel(new FlowLayout());
		southPanel.setBorder((Border) new EmptyBorder(new Insets(5, 3, 5, 3)));

		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		this.filter = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		this.orderBy = new javax.swing.JTextField();

		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setTitle("Filter & Order");
		setResizable(true);

		jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sieveicon.png")));

		jLabel2.setText("Filter (SQL 'where' condition):  ");

		jLabel3.setText("Order (comma separated field list) :  ");

		GridLayout experimentLayout = new GridLayout(0, 1);
		experimentLayout.setHgap(-1);
		experimentLayout.setVgap(4);
		JPanel questions = new JPanel(experimentLayout);
		JPanel answers = new JPanel(experimentLayout);
		questions.add(jLabel2);
		answers.add(this.filter);
		questions.add(jLabel3);
		answers.add(this.orderBy);

		// questions.add(jLabel6);
		// answers.add(ssl);

		// SOUTH
		jButton1.setText("Cancel");
		jButton1.setActionCommand("FilterCancel");

		jButton2.setText("Accept");
		jButton2.setActionCommand("FilterAccept");

		/*
		 * questions.add(jButton2); questions.add(jButton1);
		 */
		JPanel centerCenter = new JPanel(new BorderLayout());
		// JPanel centerCenterCenter = new JPanel(new BorderLayout());
		centerCenter.add(questions, BorderLayout.WEST);
		centerCenter.add(answers, BorderLayout.CENTER);
		centerCenter.add(southPanel, BorderLayout.SOUTH);
		;
		mainCenterPanel.add(centerCenter, BorderLayout.CENTER);

		JPanel buttonPane = new JPanel();
		buttonPane.add(jButton2);
		buttonPane.add(jButton1);
		mainCenterPanel.add(buttonPane, BorderLayout.SOUTH);

		mainPanel.add(jLabel1, BorderLayout.WEST);
		mainPanel.add(mainCenterPanel, BorderLayout.CENTER);
		this.add(mainPanel, BorderLayout.CENTER);

		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 500) / 2, (screenSize.height - 220) / 2, 500, 220);
	}

	// Variables declaration
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JTextField filter;
	private javax.swing.JTextField orderBy;

	public void setActionListener(Controller control) {
		jButton1.addActionListener(control);
		jButton2.addActionListener(control);

	}

	public String getOrderBy() {
		return orderBy.getText();
	}

	public void setOrderBy(String orderBy) {
		this.orderBy.setText(orderBy);
	}

	public String getFilter() {
		return filter.getText();
	}

	public void setFilter(String filter) {
		this.filter.setText(filter);

	}

}