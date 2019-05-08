package model;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.slf4j.Logger;

import logback.AreaAppender;
import model.connection.ConnectionData;
import model.connection.SQLConnector;
import model.queries.Explanation;
import model.queries.Query;
import model.relation.Database;
import model.relation.Relation;
import model.relation.RelationState;
import model.relation.Table;
import model.relation.View;
import model.tablepages.TableModelResultData;
import view.ViewInterface;

public class Model {

	private static final Logger logger = AreaAppender.getLogger(Model.class);

	/**
	 * Database connection
	 */
	SQLConnector connector = null;
	/**
	 * Active database
	 */
	Database db = null;

	/**
	 * Starts a database connection
	 * 
	 * @param cData
	 * 
	 * @return true if the connection was possible, false otherwise
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public boolean connect(ConnectionData cData) throws ClassNotFoundException, SQLException {
		boolean result = false;

		try {
			connector = new SQLConnector();
			connector.connect(cData);
			db = connector.database();
			result = db != null;
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Error during connection!" + e.getStackTrace());
			// e.printStackTrace();
		}
		return result;
	}

	/**
	 * Ends a database connection
	 * 
	 * @return true if the disconnection was possible, false otherwise
	 */
	public boolean disconnect() {
		boolean result;
		if (connector != null) {
			connector.disconnect();
			connector = null;
			result = true;
		} else
			result = true;

		return result;
	}

	/**
	 * Reset all the data
	 */
	public void reset() {

	}

	/**
	 * Help
	 */
	public void Help() {

	}

	/**
	 * @return The current database as an object of class {@code model.Database}
	 */
	public Database getDB() {

		return db;
	}

	public boolean isConnected() {
		return connector != null;
	}

	/**
	 * Changes the state of a relation.
	 * 
	 * @param r
	 *            A relation that is changing is state
	 * @param s
	 *            String representing the change. One of the values defined in
	 *            class ViewInterface
	 * @param treeModel
	 *            tree model
	 * @return A set of new buggy nodes. The input tree model can be modified
	 */
	public Set<Relation> changeNodeState(Relation r, String s, DefaultTreeModel treeModel) {
		Set<Relation> result;
		switch (s) {
		case ViewInterface.STATEINVALID:
			r.setState(RelationState.INVALID);
			break;
		case ViewInterface.STATEVALID:
			r.setState(RelationState.VALID);
			break;
		case ViewInterface.STATEUNKNOWN:
			r.setState(RelationState.UNKNOWN);
			break;
		case ViewInterface.STATETRUSTED:
			if (!r.isView())
				trustTables(treeModel);
			break;

		}

		result = checkBuggy(treeModel);

		return result;
	}

	/**
	 * Mark all the tables as valid
	 * 
	 * @param treeModel
	 *            The tree model
	 */
	private void trustTables(DefaultTreeModel treeModel) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeModel.getRoot();
		trustTables(node);

	}

	/**
	 * Mark all the tables in a tree as valid
	 * 
	 * @param node
	 *            root of the tree where the tables are looked for
	 */
	private void trustTables(DefaultMutableTreeNode node) {
		Object user = node.getUserObject();
		if (user instanceof Table) {
			Table t = (Table) user;
			t.setState(RelationState.VALID);
			logger.trace("Trusting table {}", t.getName());
		} else {
			// look in the rest of the tree
			@SuppressWarnings("unchecked")
			Enumeration<DefaultMutableTreeNode> children = (Enumeration<DefaultMutableTreeNode>) node.children();
			while (children.hasMoreElements()) {
				DefaultMutableTreeNode nextNode = children.nextElement();
				trustTables(nextNode);
			}

		}

	}

	/**
	 * Look for new buggy nodes
	 * 
	 * @param treeModel
	 * @return The set of new buggy nodes in this tree..
	 */
	private Set<Relation> checkBuggy(DefaultTreeModel treeModel) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeModel.getRoot();
		Set<Relation> result = checkBuggy(node);
		return result;

	}

	/**
	 * Recursive version of checkModel/1
	 * 
	 * @param treeModel
	 * @param node
	 * @return
	 */
	private Set<Relation> checkBuggy(DefaultMutableTreeNode node) {
		Set<Relation> result = new HashSet<Relation>();
		Object value = node.getUserObject();

		if (value instanceof Table || value instanceof View) {
			Relation r = (Relation) value;
			// to be buggy the node needs to be invalid and completely deployed
			if (r.getState() == RelationState.INVALID && r.completelyDeployed()) {
				if (isBuggyNode(node)) {
					r.setState(RelationState.BUGGY);

					result.add(r);
				}
			} else if (r.getState() == RelationState.BUGGY) {
				if (!isBuggyNode(node)) {
					logger.trace("Relation {} changed from buggy to invalid ", r.getName());
				}

			}
		}
		// look in the rest of the tree
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> children = (Enumeration<DefaultMutableTreeNode>) node.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode nextNode = children.nextElement();
			result.addAll(checkBuggy(nextNode));
		}
		return result;
	}

	/**
	 * Detects if the ode is buggy according to its children. Precond.: the node
	 * is already marked as invalid or buggy
	 * 
	 * @param node
	 *            A node containing an invalid (or buggy) relation
	 * @return True if the node is buggy, false otherwise
	 */
	private boolean isBuggyNode(DefaultMutableTreeNode node) {
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> children = (Enumeration<DefaultMutableTreeNode>) node.children();
		boolean allValid = true;
		while (allValid && children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
			Relation rChild = (Relation) child.getUserObject();
			allValid = rChild.getState() == RelationState.VALID;

		}

		return allValid;
	}

	/**
	 * Checks if the two relations have the same result set.
	 * 
	 * @param r1
	 *            First relation
	 * @param r2
	 *            Second relation
	 * @return true if the two result sets are identical
	 */
	public Explanation equalResulSet(Relation r1, Relation r2, boolean order) {
		return Query.equalResulSet(connector.getConnection(), r1, r2, order);
	}

	/**
	 * Move to the first page of the selected relation
	 * 
	 * @param r
	 * @return
	 * @throws SQLException
	 */
	public TableModelResultData moveFirst(Relation r) throws SQLException {
		TableModelResultData t = r.moveToFirstPage();
		return t;

	}

	public TableModelResultData moveNext(Relation r) throws SQLException {
		TableModelResultData t = r.moveToNextPage();
		return t;

	}

	public TableModelResultData moveLast(Relation r) throws SQLException {
		TableModelResultData t = r.moveToLastPage();
		return t;
	}

	public TableModelResultData movePrev(Relation r) throws SQLException {
		TableModelResultData t = r.moveToPrevPage();
		return t;
	}

	/**
	 * Moves the relation display to page containing the row.
	 * The row must be >=1 <=total numer of rows 
	 * @param r the displayed
	 * @param row 
	 * @return
	 * @throws SQLException
	 */
	public TableModelResultData moveTo(Relation r, long row) throws SQLException {
		TableModelResultData t = r.moveToPage(row);
		return t;
	}

	/**
	 * Indicates if two relations contain the same tuples
	 * 
	 * @param t
	 * @param r
	 * @param order
	 *            true if the order matters (the case false is not implemented
	 *            yet!)
	 * @return true if the two relations are equal, false otherwise
	 */
	public Explanation compareRelations(Relation t, Relation r, boolean order) {
		Explanation result = null;
		Connection conn = connector.getConnection();
		if (conn != null) {
			result = Query.equalResulSet(conn, t, r, order);
		}
		return result;

	}

}
