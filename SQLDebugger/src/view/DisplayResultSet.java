package view;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;

import logback.AreaAppender;
import model.tablepages.TableModelResultData;

public class DisplayResultSet extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = AreaAppender.getLogger(DisplayResultSet.class);

	private JTable table;
	private JScrollPane scrollpane;
	private TableModelResultData tm;

	public DisplayResultSet() {
		super();
		this.setLayout(new BorderLayout());
		// Border loweredetched =
		// BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		this.setBorder((Border) new EmptyBorder(new Insets(5, 5, 5, 5)));
		// this.setBorder(loweredetched);

		createTable();
		scrollpane = new JScrollPane(table);
		this.add(scrollpane, BorderLayout.CENTER);

	}

	private JTable createTable() {
		tm = new TableModelResultData();
		table = new JTable(tm);
		// if (tm!=null && tm.getColumnCount()>5)
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		/*
		 * else { TableColumnModel tcm = table.getColumnModel();
		 * tcm.getColumn(1).setMaxWidth(5); table.setColumnModel(tcm); }
		 */
		// table.setPreferredScrollableViewportSize(new Dimension(x, y));
		return table;
	}

	public void setTableModel(TableModelResultData t) {
		if (tm != null && t != null) {
			tm = t;
			table.setModel(t);
			/*
			 * table.revalidate(); table.repaint(); tm.fireTableDataChanged();
			 */
			logger.debug("Table model changed!");
		} else {
			logger.error("Table model is null!");
		}

	}

	public JTable getTable() {
		return table;
	}

	public TableModelResultData getTableModel() {
		return tm;
	}
}
