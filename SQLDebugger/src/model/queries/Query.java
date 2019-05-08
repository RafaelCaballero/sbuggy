package model.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import logback.AreaAppender;
import model.connection.ConnectionData;
import model.relation.ColumnMeta;
import model.relation.Database;
import model.relation.Relation;
import model.relation.Table;
import model.relation.View;
import model.tablepages.TableModelResultData;
import model.tuples.Tuple;

/**
 * General queries to the database. Obtaining information about user tables and
 * views. It is assumed that the connection has been established in advance.
 * 
 * @author rafa
 *
 */
public class Query {

	private static final Logger logger = AreaAppender.getLogger(Query.class);

	// system schemata are excluded
	public static List<String> sExcluded = new ArrayList<String>(Arrays.asList("pg_catalog", "information_schema"));

	public static String GETRELATIONS = "select table_name, table_schema from INFORMATION_SCHEMA.tables "; // WHERE
																											//
	public static String GETVIEWCODE = "select definition from pg_views where schemaname=? and viewname = ?";

	public static String GETSEARCHPATH = "SHOW search_path";

	/**
	 * Obtains a list of user defined views
	 * 
	 * @param conn
	 *            The current connection
	 * @return List of Strings with the name of the views.
	 */
	public static List<String> getUserRelationNames(Connection conn) {
		ArrayList<String> la = null;
		if (conn != null) {
			la = new ArrayList<String>();
			// make sure autocommit is off
			try {
				conn.setAutoCommit(false);
				Statement st;

				st = conn.createStatement();
				// Turn use of the cursor on.
				st.setFetchSize(50);
				ResultSet rs = st.executeQuery(Query.GETRELATIONS);
				while (rs.next()) {
					String tname = rs.getString(1);
					String tschema = rs.getString(2);
					if (!sExcluded.contains(tschema))
						la.add(tschema + "." + tname);
				}
				rs.close();
				// Turn the cursor off.
				st.setFetchSize(0);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return la;

	}

	/**
	 * Obtains a list of user defined views
	 * 
	 * @param conn
	 *            The current connection
	 * @return List of Strings with each schema name in the search path
	 */
	public static List<String> getSearchPath(Connection conn) {
		List<String> la = null;
		if (conn != null) {
			// make sure autocommit is off
			try {
				conn.setAutoCommit(false);
				Statement st;

				st = conn.createStatement();
				// Turn use of the cursor on.
				st.setFetchSize(50);
				ResultSet rs = st.executeQuery(Query.GETSEARCHPATH);
				if (rs.next()) {
					String tname = rs.getString(1);
					logger.info("Search path: {} ", tname);

					la = Arrays.asList(tname.split("\\s*,\\s*"));
				}
				rs.close();
				// Turn the cursor off.
				st.setFetchSize(0);

			} catch (SQLException e) {
				logger.error("Error fetching search path");
				e.printStackTrace();
			}

		}
		return la;

	}

	/**
	 * Get the Database object associated to this database. It contains all the
	 * schemas and each schema all its relations
	 * 
	 * @param conn SQL Connection
	 * @param cData Connection data (user....)
	 * @return Database representation
	 */
	public static Database getDatabase(Connection conn, ConnectionData cData) {
		Database result = null;
		List<String> ls = Query.getUserRelationNames(conn);
		List<String> searchPath = Query.getSearchPath(conn);

		if (conn != null && ls != null && ls.size() > 0) {
			try {

				PreparedStatement st = conn.prepareStatement(GETVIEWCODE);
				result = new Database(searchPath, cData);
				for (String s : ls) {
					int posdot = s.indexOf('.');
					if (posdot == -1) {
						System.out.println("Unexpected table name: " + s);
					} else {
						String schema = s.substring(0, posdot);
						String name = s.substring(posdot + 1);
						// System.out.println("Schema "+schema+" name "+name);
						// put the parameter
						st.setString(1, schema);
						st.setString(2, name);
						ResultSet rs = st.executeQuery();
						// add the new relation to the database
						if (rs.next()) {
							String def = rs.getString(1);
							View view = new View(name, schema, def, conn);
							result.put(schema, view);
						} else {
							Table table = new Table(name, schema, conn);
							result.put(schema, table);
						}

						rs.close();
					}

				}
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Get a page of a relation (a page is the set of rows currently displayed)
	 * 
	 * @param conn SQL connection
	 * @param r   Current relation
	 * @param pagenum Number of page to retrieve
	 * @return Table with the rows to show
	 * @throws SQLException if there is any problem retrieven the page data
	 */
	public static TableModelResultData getPage(Connection conn, Relation r, long pagenum) throws SQLException {
		TableModelResultData result = null;
		PreparedStatement st;
		String s0 = "";
		String s1 = "";
		// to avoid "the connection.setAutoCommit(true);" errors
		conn.setAutoCommit(true);
		boolean filter = r.getFilter() != null;
		boolean order = r.getOrderBy() != null;
		String relation = !filter ? r.fullName()
				: "(select * from " + r.fullName() + " where " + r.getFilter() + ") filterQuery";
		relation = !order ? relation : "(select * from " + relation + "  order by " + r.getOrderBy() + ") orderQuery";
		// obtain the number of rows in the relation
		s0 = "select count(*) from " + relation;

		st = conn.prepareStatement(s0);
		ResultSet rs0 = st.executeQuery();
		rs0.next();
		Long nrows = rs0.getLong(1);

		s1 = "select * from " + relation + " limit " + TableModelResultData.PAGESIZE + " offset "
				+ (pagenum * TableModelResultData.PAGESIZE);
		st = conn.prepareStatement(s1);

		ResultSet rs = st.executeQuery();
		ResultSetMetaData meta = rs.getMetaData();
		int ncolumns = meta.getColumnCount();
		result = new TableModelResultData(ncolumns + 1, pagenum, nrows);

		result.setHeader(0, "Row");
		for (int column = 1; column <= ncolumns; column++) {
			result.setHeader(column, meta.getColumnName(column));
		}

		int row = 0;
		while (rs.next() && row < TableModelResultData.PAGESIZE) {

			result.setValueAt(TableModelResultData.PAGESIZE * pagenum + row + 1, row, 0);
			for (int column = 1; column <= ncolumns; column++) {
				Object c = rs.getObject(column);
				result.setValueAt(c, row, column);
			}

			row++;
		}

		return result;
	}

	/**
	 * Checks if the two relations have the same resultset
	 * @param conn The SQL connection
	 * @param t First relation
	 * @param r Second relation
	 * @param order true if the order must be taken into account. Important: at the moment only order=false is supported.
	 * @return A explanation of the result. 
	 *  The value contains the result itself (true if the two relations are equal or not) and 
	 *  a suitable message explaining the reason of the produced result.  
	 */
	public static Explanation equalResulSet(Connection conn, Relation t, Relation r, boolean order) {
		Explanation result = null;
		if (order)
			result = equalResulSetOrder(conn, t, r);
		else
			result = equalResulSetWithoutOrder(conn, t, r);
		return result;
	}

	/**
	 * Checks if the two relations contain the same tuples, disregarding order
	 * but taking into account the number of repetitions 
	 * SELECT CASE WHEN EXISTS
	 * (TABLE a EXCEPT ALL TABLE b) OR EXISTS (TABLE b EXCEPT ALL TABLE a) THEN
	 * 'different' ELSE 'same' END AS result ;
	 * 
	 * @param conn
	 * @param t
	 * @param r
	 * @return
	 */
	private static Explanation equalResulSetWithoutOrder(Connection conn, Relation t, Relation r) {
		Explanation result = null;

		try {
			conn.setAutoCommit(true);

			Statement stAllr = conn.createStatement();
			Statement stAllt = conn.createStatement();
			ResultSet rsAllr = stAllr.executeQuery("select * from "+r.fullName());
			ResultSet rsAllt = stAllt.executeQuery("select * from "+t.fullName());
			ResultSetMetaData metar = rsAllr.getMetaData();
			ResultSetMetaData metat = rsAllt.getMetaData();
			
			ColumnMeta arrayMetar[] = getColumnMetas(metar);
			Set<ColumnMeta> cr = new HashSet<ColumnMeta>(Arrays.asList(arrayMetar));
			ColumnMeta arrayMetat[] = getColumnMetas(metat);
			Set<ColumnMeta> ct = new HashSet<ColumnMeta>(Arrays.asList(arrayMetat));
			
			cr.retainAll(ct);
			

			if (cr.size()==0) {
				result = new Explanation(false, true, "Error comparing relations " + t.fullName() + " and "
						+ r.fullName() + ": no common columns!");
	
			} else {
			// create the select 
			int ncolumns = cr.size();
			int i=0;
			String columns = "";
			for(ColumnMeta c:cr) {
				columns+=c.getColumnName();
				i++;
				if (i<ncolumns)
					columns+=",";
			}

			String s = "SELECT CASE WHEN EXISTS (select "+columns+" from " + t.fullName() + 
					                             " EXCEPT ALL select "+columns+" from "
					+ r.fullName() + ")  OR " + 
					                    "EXISTS (select "+columns+" from " + r.fullName() + 
					                             " EXCEPT ALL select "+columns+" from "
					+ t.fullName() + ") THEN 'different' ELSE 'equal' END";
			
			Statement st = conn.createStatement();
			ResultSet rs0 = st.executeQuery(s);
			rs0.next();
			String sresult = rs0.getString(1);
			if (sresult.equals("equal")) {
				result = new Explanation(true, "Identical rows, identical number of repetitions (order not considered");
			} else {
				
				// we can go further
				String s1 = "select count(*) from (select "+columns+" from " + t.fullName() + " EXCEPT ALL select "+columns+" from "
						+ r.fullName()+") A";
				String s2 = "select count(*) from (select "+columns+" from " + r.fullName() + " EXCEPT ALL select "+columns+" from "
						+ t.fullName()+") B";
				ResultSet rs1 = st.executeQuery(s1);
				rs1.next();
				Long n1 = rs1.getLong(1);
				ResultSet rs2 = st.executeQuery(s2);
				rs2.next();
				Long n2 = rs2.getLong(1);
				String explain = "";
				// number of rows in t not in r
				if(n1>0)
					explain = t.fullName() + " contains "+ n1 +" rows not in "+r.fullName();
				// number of rows in r not in t
				if(n2>0)
					explain = r.fullName() + " contains "+ n2 +" rows not in "+t.fullName();
				
				result = new Explanation(false, explain);
			}
			}
		} catch (SQLException e) {
			String msg = e.getMessage();
			logger.error("Error comparing relation {} and {}", t.fullName(), r.fullName());
			if (msg.contains("each EXCEPT query must have the same number of columns"))
				result = new Explanation(false, true, "Error comparing relations " + t.fullName() + " and "
						+ r.fullName() + ": different number of columns!");
			else
				result = new Explanation(false, true,
						"Error comparing relations " + t.fullName() + " and " + r.fullName());
		}

		return result;
	}

	/**
	 * Obtain a set with all the columns of a query
	 * @param metar Result metadata of the query
	 * @return The set containing the columns of the query
	 */
	private static ColumnMeta[] getColumnMetas(ResultSetMetaData metar) {
		int ncolumns;
		ColumnMeta[] result;
		try {
			ncolumns = metar.getColumnCount();
			result = new ColumnMeta[ncolumns];
			for (int i=1; i<=ncolumns; i++) {
				int typeC = metar.getColumnType(i);
				String nameC = metar.getColumnName(i);
				result[i-1] = new ColumnMeta(typeC,nameC);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Error obtaining the query meta data");
			result = null;
		}
		
		
		return result;
	}

	/**
	 * Checks if the two relations have the same result set, considering that
	 * order matters IMPORTANT: not finished yet!!!!
	 * 
	 * @param conn The SQL connection
	 * @param t
	 *            First relation (the trusted relation)
	 * @param r
	 *            Second relation (the debugee)
	 * @return true if the two result sets are identical
	 */
	public static Explanation equalResulSetOrder(Connection conn, Relation t, Relation r) {
		Explanation result = null;
		boolean identical = true;
		try {
			conn.setAutoCommit(false);
			Statement st1;
			Statement st2;

			st1 = conn.createStatement();
			st2 = conn.createStatement();
			// Turn use of the cursor on.
			st1.setFetchSize(50);
			ResultSet rs1 = st1.executeQuery("select * from " + t.fullName());
			st2.setFetchSize(50);
			ResultSet rs2 = st1.executeQuery("select * from " + r.fullName());
			long row = 0;
			while (identical && rs1.next()) {
				row++;
				Tuple trepr = tupleRepr(rs1);
				if (!rs2.next()) {
					identical = false;
					String explain = "Tuple number " + row + ": " + trepr + " of " + t.fullName() + " not in "
							+ r.fullName();
					logger.info(explain);
					result = new Explanation(false, explain);
				} else { // compare the two rows
				}
			}
			// rs2 has more elements?
			if (identical && rs2.next()) {
				identical = false;
				Tuple trepr = tupleRepr(rs2);
				String explain = "Tuple number " + row + ": " + trepr + " of " + r.fullName() + " not in "
						+ t.fullName();
				logger.info(explain);
				result = new Explanation(false, explain);
			}

			rs1.close();
			rs2.close();
			// Turn the cursor off.
			st1.setFetchSize(0);
			st2.setFetchSize(0);

			if (identical) {
				result = new Explanation(false, "Same tuples in the same order");
				logger.info("Relations {} and {}: same tuples in the same order", t.fullName(), r.fullName());
			}
		} catch (SQLException e) {
			logger.error("Error comparing relation {} and {}", t.fullName(), r.fullName());
			result = null;
		}

		return result;
	}

	/**
	 * String representation of a tuple
	 * 
	 * @param rs
	 *            Resultset pointing to a valid tuple
	 * @return
	 * @throws SQLException
	 */
	private static Tuple tupleRepr(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int ncolumns = meta.getColumnCount();
		Tuple result = new Tuple(ncolumns);
		for (int column = 1; column <= ncolumns; column++) {
			// TODO String cname = meta.getColumnName(column);

		}

		return result;
	}

}
