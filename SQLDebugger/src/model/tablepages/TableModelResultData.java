package model.tablepages;

import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableModelResultData extends DefaultTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(TableModelResultData.class);

	public static int PAGESIZE = 15;
	private Object[][] page;
	private String[] headers;
	private long numpage;
	private int numcolumns;
	/**
	 * Total number of rows in this relation
	 */
	long numrows;

	public TableModelResultData(int columns, long numpage, long nrows) {
		this.numpage = numpage;
		this.numrows = nrows;
		this.numcolumns = columns;
		page = new Object[PAGESIZE][numcolumns];
		headers = new String[numcolumns];
	}

	public TableModelResultData() {
		this.numpage = 0;
		this.numrows = 0L;
		this.numcolumns = 0;
		initEmpty();
	}

	private void initEmpty() {
		this.numpage = 0;
		this.numrows = 0L;
		this.numcolumns = 5;
		page = new Object[PAGESIZE][numcolumns];
		headers = new String[numcolumns];
		for (int i = 0; i < numcolumns; i++)
			headers[i] = "header" + "i";
		for (int i = 0; i < PAGESIZE; i++)
			for (int j = 0; j < numcolumns; j++)
				page[i][j] = "i:" + i + " j:" + j;

	}

	/**
	 * @return Total number of rows in this relation
	 */
	public long getTotalRows() {

		return numrows;
	}

	@Override
	public int getRowCount() {

		return TableModelResultData.PAGESIZE;
	}

	@Override
	public int getColumnCount() {

		return numcolumns;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;
		if (!correctindices(rowIndex, columnIndex))
			logger.error("Indices out of range in getValueAt. Row: {},  Column: {}", rowIndex, columnIndex);
		else
			result = page[rowIndex][columnIndex];
		return result;
	}

	public void copyRows(TableModelResultData tm) {
		this.page = tm.page;
		this.numcolumns = tm.numcolumns;
		this.numrows = tm.numrows;
	}

	@Override
	public void setValueAt(Object o, int rowIndex, int columnIndex) {
		if (correctindices(rowIndex, columnIndex))
			page[rowIndex][columnIndex] = o;
		else
			logger.error("Indices out of range in setValueAt. Row: {},  Column: {}", rowIndex, columnIndex);
	}

	private boolean correctindices(int rowIndex, int columnIndex) {
		boolean correct = rowIndex >= 0 && rowIndex < TableModelResultData.PAGESIZE && columnIndex >= 0
				&& columnIndex < numcolumns;
		return correct;

	}

	@Override
	public String getColumnName(int index) {
		return headers[index];
	}

	public void setHeader(int i, String columnName) {
		headers[i] = columnName;

	}

	public long getNumPage() {
		return numpage;
	}

	/**
	 * Maximum number of pages. Obtained as the integer part of numrows/PAGESIZE
	 * 
	 * @return numrows/PAGESIZE
	 */
	public long getMaxPages() {
		// TODO Auto-generated method stub
		return numrows / (long) PAGESIZE;
	}

}
