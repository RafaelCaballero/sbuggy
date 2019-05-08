package model.relation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

//import org.slf4j.Logger;

//import logback.AreaAppender;
import model.queries.Query;
import model.tablepages.TableModelResultData;

public abstract class Relation {

	// private static final Logger logger =
	// AreaAppender.getLogger(Relation.class);

	/**
	 * Schema name
	 */
	private String schema;

	/**
	 * relation name
	 */
	private String name;

	/**
	 * Current filter. The filter is used to display just the rows meeting some
	 * SQL condition
	 */
	private String filter = null;

	/**
	 * Display the relation rows in a given order
	 */

	private String orderBy = null;

	private TableModelResultData t = null;

	private Connection conn;

	protected boolean deployed;

	protected Set<Relation> subrelations;

	protected RelationState state;

	/**
	 * Creates a new relation with name {@code name}
	 * 
	 * @param name
	 *            The relation name
	 */
	public Relation(String name, String schema, Connection conn) {
		this.name = name;
		this.schema = schema;
		this.conn = conn;
		this.deployed = false;
		this.subrelations = new HashSet<Relation>();
		state = RelationState.UNKNOWN;
	}

	/**
	 * Copy constructor
	 */
	public Relation(Relation r) {
		this.name = r.name;
		this.schema = r.schema;
		this.conn = r.conn;
		this.deployed = r.deployed;
		this.subrelations = new HashSet<Relation>();
		for (Relation rprime : r.getSubrelations())
			if (rprime.isView())
				this.subrelations.add(new View(rprime));
			else
				this.subrelations.add(new Table(rprime));
		this.state = r.getState();
		this.filter = r.getFilter();
		this.orderBy = r.getOrderBy();

	}

	/**
	 * @return true if this relation is a view
	 */
	public abstract boolean isView();

	public abstract String getIcon();

	public String getName() {
		return name;
	}

	public String getSchema() {
		return schema;
	}

	@Override
	public String toString() {

		return fullName();
	}

	public String fullName() {
		return getSchema() + "." + getName();
	}

	/**
	 * 
	 * @return a table model object representing the current page
	 * @throws SQLException
	 */
	public TableModelResultData getTableModel() throws SQLException {
		// first access, prepare the pages to display
		if (t == null) {
			initTableModel();
		}
		return t;
	}

	private void initTableModel() throws SQLException {
		// return the first page
		t = Query.getPage(conn, this, 0);
	}

	/**
	 * @return true if the subrelations of this relation have been already
	 *         obtained
	 */
	public boolean isDeployed() {
		return deployed;
	}

	/**
	 * Completely deployed means deployed and with all its relations deployed.
	 * 
	 * @return true if the relation is completely deployed
	 */
	public boolean completelyDeployed() {
		boolean result = deployed;

		if (deployed && subrelations != null) {
			Relation array[] = new Relation[subrelations.size()];

			array = subrelations.toArray(array);
			// logger.trace("Array of subrelations: {}", (Object [])array);
			for (int i = 0; result && i < array.length; i++) {
				result = result && array[i].completelyDeployed();
			}
		}

		return result;
	}

	/**
	 * @return Set of subrelations, root of all the subtrees in the dependency
	 *         tree. Null if isDeployed()==false
	 */
	public Set<Relation> getSubrelations() {
		return subrelations;
	}

	/**
	 * All the relations contained in this dependency tree
	 * 
	 * @return A set containing all the relations in the dependency tree
	 *         including this relation
	 */
	public Set<Relation> getAllSubrelations() {
		Set<Relation> result = null;
		if (completelyDeployed()) {
			result = getAllSubrelationsCompletelyDeployed();
		}

		result.add(this);
		return result;

	}

	/**
	 * Obtains all the relations in this dependence tree. Precond.: the relation
	 * is completely deployed
	 * 
	 * @return All the subrelations in this dependence tree
	 */
	private Set<Relation> getAllSubrelationsCompletelyDeployed() {
		Set<Relation> result = new HashSet<Relation>();
		result.add(this);
		if (subrelations != null) {

			Relation array[] = new Relation[subrelations.size()];

			array = subrelations.toArray(array);
			// logger.trace("Array of subrelations: {}", (Object [])array);
			for (int i = 0; i < array.length; i++) {
				result.addAll(array[i].getAllSubrelationsCompletelyDeployed());
			}

		}

		return result;
	}

	/**
	 * Deploy the current relation obtaining all its subrelations with respect
	 * to the Database db
	 * 
	 * @param db
	 * @return Set of deployed relations (excluding this one)
	 */
	public abstract Set<Relation> deploy(Database db);

	public void completelyDeploy(Database db) {
		deploy(db);
		for (Relation subr : getSubrelations())
			if (!subr.completelyDeployed())
				subr.completelyDeploy(db);

	}

	public void setState(RelationState state) {
		this.state = state;
	}

	public RelationState getState() {
		return state;
	}

	/**
	 * Checks if the dependence tree contains some relation with the give name
	 * disregarding the schema.
	 * 
	 * @param name
	 * @return true if the dependence tree with this relation as root contains
	 *         any relation with name 'name' disregarding the schema
	 */
	public boolean contains(String name) {
		boolean result = false;
		if (name.equals(getName()))
			result = true;
		else {
			Set<Relation> subrels = this.getSubrelations();

			for (Relation r : subrels) {
				if (r.contains(name))
					return true;
			}
		}

		return result;
	}

	/**
	 * Move to the first page
	 * 
	 * @return The TableModelResultData
	 * @throws SQLException
	 */
	public TableModelResultData moveToFirstPage() throws SQLException {
		initTableModel();

		return t;
	}

	/**
	 * Move to the first page
	 * 
	 * @return The TableModelResultData
	 * @throws SQLException
	 */
	public TableModelResultData moveToNextPage() throws SQLException {
		long npage = t.getNumPage();
		long max = t.getMaxPages();
		if (npage < max)
			t = Query.getPage(conn, this, npage + 1);
		return t;
	}

	public TableModelResultData moveToLastPage() throws SQLException {
		long npage = t.getNumPage();
		long max = t.getMaxPages();
		if (npage != max)
			t = Query.getPage(conn, this, max);
		return t;
	}

	public TableModelResultData moveToPrevPage() throws SQLException {
		long npage = t.getNumPage();
		if (npage != 0)
			t = Query.getPage(conn, this, npage - 1);
		return t;
	}

	/**
	 * Moves the displayed page
	 * 
	 * @param row
	 *            The row where the table must be moved. Important: the row is
	 *            1 based (not zero based!!)
	 * @return The table model obtained after the movement
	 * @throws SQLException
	 */
	public TableModelResultData moveToPage(long row) throws SQLException {
		row--;
		long max = t.getTotalRows();
		long pagesize = t.getRowCount();
		long page = (row/pagesize); 
		if (row <= max && row >= 0)
			t = Query.getPage(conn, this, page);
		return t;
	}

	/**
	 * The filter of this relation. Null if there is no active filter
	 * 
	 * @return Current filter of the relation
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * Replaces the current filter by the new filter
	 * 
	 * @param newFilter
	 */
	public void setFilter(String newFilter) {
		this.filter = newFilter;
	}

	/**
	 * @return Current order, null if no order has been specified
	 */
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * Replaces the current order by a new order
	 * 
	 * @param newOrder
	 *            the new order
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

}
