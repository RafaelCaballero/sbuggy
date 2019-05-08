package view;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;

import conf.AppConf;
import view.statusbar.StatusBarMessage;
import control.Controller;
import model.connection.ConnectionData;
import model.relation.Database;
import model.relation.Relation;
import model.relation.View;
import model.tablepages.TableModelResultData;

/**
 * This interface serves as a bridge between the view and the controller
 * @author rafa
 *
 */
public interface ViewInterface {

	static public final String CONNECT = "Connect";
	static public final String DISCONNECT = "Disconnect";
	static public final String ABOUT = "About";
	static public final String RESET = "Reset";
	static public final String HELP = "Help";
	static public final String LOGINEXIT = "LoginExit";
	static public final String LOGINLOGIN = "LoginLogin";
	static public final String STATEVALID = "Valid";
	static public final String STATEINVALID = "Invalid";
	static public final String STATETRUSTED = "Trusted";
	static public final String STATEUNKNOWN = "Unknown";
	static public final String FILTER = "Filter & Order";
	static public final String FILTERACCEPT = "FilterAccept";
	static public final String FILTERCANCEL = "FilterCancel";
	static public final String GOTOROW = "GotoPage";
	static public final String TABLEFIRST = "TableFirst";
	static public final String TABLELAST = "TableLast";
	static public final String TABLEPREV = "TablePrev";
	static public final String TABLENEXT = "TableNext";

	/**
	 * @param control
	 *            : the controller
	 */
	void setController(Controller control);

	// show the window
	void start();

	// shows the contextual menu
	void showMenu();

	/**
	 * Displays a message in the status bar
	 * 
	 * @param msg
	 *            message to display
	 */
	void status(String msg);

	void status(StatusBarMessage msg);

	/**
	 * Asks for the connection data and returns the data.
	 * 
	 * @return A Properties object with the connection data or null if the user
	 *         canceled the operation.
	 */
	ConnectionData getConectionData();

	/**
	 * Perform changes after a connection
	 * 
	 * @param props
	 *            Properties defining the connection
	 * @param db
	 *            database
	 */
	void setConnected(ConnectionData props, Database db, TreeSelectionListener listen);

	/**
	 * Opens the login form. Employed to connect to the database
	 */
	void openLoginForm();

	/**
	 * Closes the login form
	 */
	void closeLoginForm();

	/******************** errors *************************/
	void displayError(String msg, Exception e);

	/**
	 * Gets the login information
	 * @return The application configuration modified after the login process 
	 */
	AppConf getLoginConf();

	/**
	 * true if the login information must be saved
	 * @return true if the configuration has been successfully saved, false otherwise
	 * @throws IOException An IO error occurred writing the configuration file
	 */
	boolean saveLogin(AppConf conf) throws IOException;

	/**
	 * Displays the result of a relation
	 */
	void displayRelation(TableModelResultData r);

	/**
	 * refresh the database tree
	 * 
	 */
	void refreshTree();

	/**
	 * Returns the selected relation in the tree
	 * 
	 * @return The selected relation in the tree, or null if there is no
	 *         selected relation
	 */
	Relation getSelectedRelation();

	/**
	 * Obtain the model of the tree of views and tables
	 * 
	 * @return The current model of the tree
	 */
	DefaultTreeModel getTreeModel();

	/**
	 * Changes the model of the tree of relations
	 * 
	 * @param treemodel
	 *            New Tree model
	 */
	void setTreeModel(DefaultTreeModel treemodel);

	/**
	 * Report to the user that a new buggy relation has been found
	 * 
	 * @param buggy
	 */
	void informBuggy(Relation buggy);

	/**
	 * Indicate that a relation is trusted
	 * 
	 * @param trusted
	 */
	void setTrusted(View trusted);

	/**
	 * Displays the source code of a view
	 * 
	 * @param string
	 *            The sourcecode of the view
	 */
	void showSource(String string);

	/**
	 * Allows the user to input a filter for the current select relation
	 * 
	 * @param fullname
	 *            Full name of the relation
	 * @param filter
	 *            current filter
	 * @param orderBy
	 *            Current order in the relation
	 */
	void openFilterForm(String fullName, String filter, String orderBy);

	/**
	 * Closes the filter&Sort form
	 */
	void closeFilterForm();

	/**
	 * Filter defined by the user in the filter form
	 * @return The filter defined by the user
	 */
	String getFilter();

	String getOrderBy();

	/**
	 * Displays help
	 */
	void help();

	/**
	 * Display about dialog
	 */
	void about();

	/**
	 * @return The main application frame
	 */
	JFrame getFrame();
}
