package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

//import org.slf4j.Logger;

import control.Controller;
//import logback.AreaAppender;
import model.tablepages.TableModelResultData;

public class DisplayRelation extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane epane = null;
	// private static final Logger logger =
	// AreaAppender.getLogger(DisplayRelation.class);
	private JPanel south;
	private JButton first;
	private JButton next;
	private JButton prev;
	private JButton last;
	private JPanel buttonsPanel;
	private JPanel westPanel;
	private static int sizex = 10;
	private static int sizey = sizex;
	private static Dimension buttonDim = new Dimension(sizex, sizey);
	private JTextField editRow;
	private JLabel totalRows;
	/**
	 * For controlling the navigation buttons
	 */
	private Controller control;

	public DisplayRelation() {
		super(new BorderLayout());
		setBorder((Border) new EmptyBorder(new Insets(1, 1, 1, 1)));
		setPreferredSize(new Dimension(250, -1));

	}

	public void display(DisplayResultSet drs) {
		if (drs != null) {
			JTable table = drs.getTable();
			epane = new JScrollPane(table);
			add(epane);

			// south panel contains information and buttons for navigation
			south = new JPanel(new BorderLayout());
			south.setBorder((Border) new EmptyBorder(new Insets(1, 1, 1, 1)));

			TableModelResultData tm = drs.getTableModel();
			Long nrows = tm.getTotalRows();

			Long page = tm.getNumPage();
			int pagesize = tm.getRowCount();

			JPanel center = new JPanel(new GridLayout(1, 3));
			center.setBorder((Border) new EmptyBorder(new Insets(10, 1, 10, 1)));
			JLabel labelRow = new JLabel("Row ");
			labelRow.setHorizontalAlignment(SwingConstants.RIGHT);
			center.add(labelRow);
			editRow = new JTextField((page*((long)pagesize) + 1L) + "");
			editRow.setActionCommand(ViewInterface.GOTOROW);
			editRow.addActionListener(control);
			totalRows = new JLabel("/" + (nrows));
			center.add(editRow);
			center.add(totalRows);
			south.add(center, BorderLayout.CENTER);

//			JPanel east = new JPanel(new BorderLayout());
//			total = new JLabel("Rows: " + nrows);
//			east.add(total);
//			south.add(east, BorderLayout.EAST);

			westPanel();
			south.add(westPanel, BorderLayout.WEST);
			add(south, BorderLayout.SOUTH);

		}
	}

	private void westPanel() {

		westPanel = new JPanel(new BorderLayout());
		// buttons
		buttonsPanel = new JPanel();
		ImageIcon ImgFirst = new ImageIcon(getClass().getResource("/resources/first.png"));
		first = new JButton();
		first.setSize(buttonDim);
		first.setIcon(ImgFirst);
		first.setActionCommand(ViewInterface.TABLEFIRST);
		first.addActionListener(control);
		first.setToolTipText("First page");

		buttonsPanel.add(first);

		ImageIcon ImgPrev = new ImageIcon(getClass().getResource("/resources/prev.png"));
		prev = new JButton();
		prev.setSize(buttonDim);
		prev.setIcon(ImgPrev);
		prev.setActionCommand(ViewInterface.TABLEPREV);
		prev.addActionListener(control);
		prev.setToolTipText("Previous page");
		buttonsPanel.add(prev);

		ImageIcon ImgNext = new ImageIcon(getClass().getResource("/resources/next.png"));

		next = new JButton();
		next.setSize(buttonDim);
		next.setIcon(ImgNext);
		next.setActionCommand(ViewInterface.TABLENEXT);
		next.addActionListener(control);
		next.setToolTipText("Next page");
		buttonsPanel.add(next);

		ImageIcon ImgLast = new ImageIcon(getClass().getResource("/resources/last.png"));
		last = new JButton();
		last.setIcon(ImgLast);
		last.setActionCommand(ViewInterface.TABLELAST);
		last.addActionListener(control);
		last.setToolTipText("Last page");
		buttonsPanel.add(last);

		westPanel.add(buttonsPanel, BorderLayout.WEST);

	}

	public void update(DisplayResultSet drs) {
		if (drs != null) {
			TableModelResultData tm = drs.getTableModel();
			Long nrows = tm.getTotalRows();
			int pagesize = tm.getRowCount();
			//total.setText("Rows: " + nrows);
			Long page = tm.getNumPage();
			editRow.setText(page*pagesize + 1L + "");
			totalRows.setText("/" + (nrows));
		}

	}

	/**
	 * The action listener is used for the navigation buttons. Initially is
	 * simply stored, and it is assigned to the buttons when they are actually
	 * created.
	 * 
	 * @param control
	 */
	public void mySetActionListener(Controller control) {
		this.control = control;

	}

}
