package control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;

import conf.AppConf;
import logback.AreaAppender;
import model.DeployInBackGround;
import model.Model;
import model.connection.ConnectionData;
import model.relation.Database;
import model.relation.Relation;
import model.relation.RelationState;
import model.relation.Table;
import model.relation.View;
import model.tablepages.TableModelResultData;
import view.ViewInterface;
import view.statusbar.StatusBarMessage;

/**
 * The controller in the model–view–controller design pattern. Detects the user
 * actions, informs the model and refreshes the view.
 * 
 * @author rafa
 *
 */
public class Controller implements ActionListener, MouseListener, TreeSelectionListener {
	private ViewInterface view;
	private Model mod;
	private boolean debugging;
	/**
	 * Trusted specification
	 */
	private View trusted;

	private static final Logger logger = AreaAppender.getLogger(Controller.class);

	public Controller(ViewInterface view, Model mod) {
		this.view = view;
		this.mod = mod;
		this.debugging = false;
		this.trusted = null;
		logger.info("Application started");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();

		switch (s) {
		case ViewInterface.CONNECT:
			// connect to a database with a username and passwd.
			view.status("");
			view.openLoginForm();
			debugging = true;
			break;
		case ViewInterface.DISCONNECT:
			debugging = false;
			boolean isDisconnected = mod.disconnect();
			if (isDisconnected) {
				view.status("Not Connected");
				mod.disconnect();
				view.openLoginForm();
				debugging = true;

			}
			break;
		case ViewInterface.TABLEFIRST:
			// move to the first page of the displayed relation
			Relation r = view.getSelectedRelation();
			TableModelResultData t = null;
			try {
				t = mod.moveFirst(r);
			} catch (SQLException ex) {
				view.displayError("SQL error " + ex.getMessage(), null);
				logger.error("Error getting page for relation {}: {}", r.fullName(), ex.getStackTrace());
			}

			view.displayRelation(t);
			break;
		case ViewInterface.TABLELAST:
			// move to the last page of the displayed relation
			r = view.getSelectedRelation();
			try {
				t = mod.moveLast(r);
			} catch (SQLException ex) {
				view.displayError("SQL error " + ex.getMessage(), null);
				logger.error("Error getting page for relation {}: {}", r.fullName(), ex.getStackTrace());
				t = null;
			}
			view.displayRelation(t);
			break;

		case ViewInterface.TABLEPREV:
			// move to the prev page of the displayed relation
			r = view.getSelectedRelation();
			try {
				t = mod.movePrev(r);
			} catch (SQLException ex) {
				view.displayError("SQL error " + ex.getMessage(), null);
				logger.error("Error getting page for relation {}: {}", r.fullName(), ex.getStackTrace());
				t = null;
			}

			view.displayRelation(t);
			break;
		case ViewInterface.TABLENEXT:
			// move to the next page of the displayed relation
			r = view.getSelectedRelation();
			try {
				t = mod.moveNext(r);
			} catch (SQLException ex2) {
				view.displayError("SQL error " + ex2.getMessage(), null);
				logger.error("Error getting page for relation {}: {}", r.fullName(), ex2.getStackTrace());
				t = null;
			}

			view.displayRelation(t);
			break;
		case ViewInterface.GOTOROW:
			// move to the page indicated by the user of the displayed relation
			r = view.getSelectedRelation();
			JTextField textFieldRow = (JTextField) e.getSource();
			String row = textFieldRow.getText();
			try {
				int rownum = Integer.parseInt(row);
				try {
					t = mod.moveTo(r, rownum);
				} catch (SQLException ex) {
					view.displayError("SQL error " + ex.getMessage(), null);
					logger.error("Error getting page for relation {}: {}", r.fullName(), ex.getStackTrace());
					t = null;
				}

				view.displayRelation(t);
			} catch (NumberFormatException excep) {
				view.displayError("Invalid row number " + row, null);
				logger.trace("Error, invalid row number {}", row);

				try {
					t = r.getTableModel();
				} catch (SQLException ex) {
					view.displayError("SQL error " + ex.getMessage(), null);
					logger.error("Error getting page for relation {}: {}", r.fullName(), ex.getStackTrace());
					t = null;
				}

			}
			view.displayRelation(t);
			break;
		case ViewInterface.RESET:
			loginControl(true);
			break;
		case ViewInterface.HELP:
			view.help();
			break;
		case ViewInterface.ABOUT:
			view.about();
			break;
		case ViewInterface.LOGINEXIT:
			view.closeLoginForm();
			if (!mod.isConnected())
				view.status(new StatusBarMessage("", "Click on the upper-right icon to start"));
			break;
		case ViewInterface.FILTER:
			r = view.getSelectedRelation();
			view.status("Set relation filter");
			String filter = r.getFilter();
			String orderBy = r.getOrderBy();
			view.openFilterForm(r.fullName(), filter, orderBy);
			break;
		case ViewInterface.FILTERACCEPT:
			view.closeFilterForm();

			filter = view.getFilter();
			if (filter.length() == 0)
				filter = null;
			orderBy = view.getOrderBy();
			if (orderBy.length() == 0)
				orderBy = null;
			r = view.getSelectedRelation();
			r.setFilter(filter);
			r.setOrderBy(orderBy);
			// display the relation
			try {
				t = mod.moveFirst(r);
			} catch (SQLException ex) {
				view.displayError("SQL error " + ex.getMessage(), null);
				logger.error("Error getting page for relation {}: {}", r.fullName(), ex.getStackTrace());
				t = null;
			}

			view.displayRelation(t);
			break;
		case ViewInterface.FILTERCANCEL:
			view.closeFilterForm();
			break;

		case ViewInterface.STATEVALID:
		case ViewInterface.STATEINVALID:
		case ViewInterface.STATEUNKNOWN:
		case ViewInterface.STATETRUSTED:
			r = view.getSelectedRelation();
			if (r.isView() && s.equals(ViewInterface.STATETRUSTED)) {
				trusted = (View) r;
				logger.info("View {} selected as trusted specification", r.fullName());
				view.status("Select a view to debug (same name but different schema)");
				view.setTrusted(trusted);
			} else
				changeNodeState(r, s);
			break;

		case ViewInterface.LOGINLOGIN:
			loginControl(true);
			break;
		default:
			logger.error("Unexpected menu Error! (getActionCommand: {})", s);
		}
	}

	/**
	 * This method is called when the state of a relation is changed
	 * 
	 * @param r
	 *            Relation that is affected by the change
	 * @param s
	 *            new state
	 */
	private void changeNodeState(Relation r, String s) {
		/*
		 * DeployInBackGround deployb =
		 * mod.getDB().getDeployingProcess(r.fullName()); if (deployb == null) {
		 * deploy(r, mod.getDB()); deployb =
		 * mod.getDB().getDeployingProcess(r.fullName()); } if (deployb == null)
		 * { if (r instanceof View) logger.trace(
		 * "Error: implossible to deploy {} at Controller.changeNodeState",
		 * r.fullName()); }
		 */
		// try {
		/*
		 * if (deployb!=null) deployb.get();
		 */
		DefaultTreeModel treemodel = view.getTreeModel();
		Set<Relation> newBuggyNodes = mod.changeNodeState(r, s, view.getTreeModel());
		view.setTreeModel(treemodel);
		// inform to the user of the new buggy nodes detected
		for (Relation buggy : newBuggyNodes)
			view.informBuggy(buggy);

		// } catch (InterruptedException | ExecutionException e) {
		// logger.error("Error deploying relation {} at
		// Controller.changeNodeState", r.fullName());
		// }

	}

	private void loginControl(boolean save) {
		// close the login form
		view.closeLoginForm();
		if (save) {
			// save the login data to the configuration
			AppConf conf = view.getLoginConf();
			// save the configuration
			try {
				view.saveLogin(conf);
				logger.info("Saved configuration in file {} ", conf.getPath());
			} catch (IOException e1) {
				logger.error("Error saving configuration " + e1.getMessage());
				e1.printStackTrace();
			}
		}
		// ask for the connection data
		ConnectionData cData = view.getConectionData();
		if (cData != null) {
			try {
				boolean success = mod.connect(cData);
				if (!success) {
					view.displayError("Connection error: database empty or not found! ", null);
					logger.error("Database empty or not found!");

				} else
					view.setConnected(cData, mod.getDB(), this);
			} catch (ClassNotFoundException e) {
				view.displayError("Driver not found ", e);
				logger.error("Driver not found!");
			} catch (SQLException ex) {
				view.displayError("Connection failed ", ex);
				logger.error("Connection failed. \n {} ", cData);
			}
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {

		// show contextual menu
		view.showMenu();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * A new selected relation (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.
	 * TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();
		if (path != null) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			Relation r = (Relation) node.getUserObject();

			if (r instanceof View) {
				view.showSource(((View) r).getDef());
			} else
				view.showSource("");

			// the relation might be a non-deployed view
			view.status("Deploying " + r.getName());

			try {
				// display the selection in the status bar
				TableModelResultData rModel = r.getTableModel();
				view.displayRelation(rModel);
			} catch (SQLException ex) {
				view.displayError("SQL error " + ex.getMessage(), null);
				logger.error("Error getting page for relation {}: {}", r.fullName(), ex.getStackTrace());
			}

			deploy(r, mod.getDB());

			/*
			 * if (r.isView() && !r.isDeployed()) view.status("Deploying "
			 * +r.getName());
			 * 
			 * // deployment if (!r.isDeployed()) { Set<String> srls =
			 * r.deploy(); }
			 */
			view.status(selectedStatus(r));
			// if we have already selected a trusted specification...
			if (trusted != null) {

				checkTrusted(r, mod);
			}

		} else {
			view.status("");
			view.showSource("");

			// view.displayRelation(null);
		}

	}

	/**
	 * A trusted specification debugging. The trusted version is on attribute
	 * trusted. It is used to debug relation r and the relations contained in
	 * its dependency tree
	 * 
	 * @param r
	 *            The relation to debug.
	 */
	public void checkTrusted(Relation r, Model mod) {
		String trustedSpecName = trusted.getName();
		String trustedSpecFullName = trusted.fullName();
		trusted = null; // to avoid recursive calls
		view.setTrusted(null);
		// a copy of trustedSpec
		View trustedSpec = (View) mod.getDB().getRelation(trustedSpecFullName);
		if (r.getName().equals(trustedSpecName)) {

			// the name is ok, what about the schema?
			if (r.fullName().equals(trustedSpecFullName)) {
				// the same relation chosen twice, we just ignore it
				view.displayError("A relation cannot be its own trusted specification!", null);
				logger.error("A relation cannot be its own trusted specification!");
				logger.info("Cancelling trusted specification");

			} else {
				view.status("Checking trusted specification, please wait");
				TrustedSpecification ts = new TrustedSpecification(trustedSpec, r, mod,view);
				String longExplanation = "Automated debugging of "+r.fullName()+" with trusted specification "+r.fullName()+"\n";
				longExplanation += "==========================\n";
				
				List<StateChange> ls = ts.start();
				int valid=0;
				int invalid=0;
				for (StateChange changeRelation : ls) {
					
					Relation cr = changeRelation.getR();
					RelationState newState = changeRelation.getState();
					longExplanation += "     " + cr.fullName() + " changes from "+ cr.getState() +" to " + newState+"\n";
					if (changeRelation.getExplanation().length()>0)
					   longExplanation += "      (" + changeRelation.getExplanation()+")\n";
					longExplanation += "\n";
					String theState = newState.toString();
					changeNodeState(cr, theState);
					if (newState==RelationState.VALID)
						valid++;
					if (newState==RelationState.INVALID)
						invalid++;
					
				}
				
				longExplanation += "\n Summary: "+(valid+invalid)+" relations changed, "+valid + " to valid, "+invalid +" to invalid"+"\n";
				longExplanation += "==========================\n";
				logger.info(longExplanation);

			}

		} else {
			view.status("Error, selected relation " + r.fullName() + " does not match trusted specification "
					+ trustedSpec.fullName());
			view.setTrusted(null);
			logger.error("Error, selected relation {} does not match trusted specification {}", r.fullName(),
					trustedSpec.fullName());
			logger.info("Cancelling trusted specification");
		}
	}

	/**
	 * Expand the tree of subrelations of the relation recursively:
	 * <ul>
	 * <li>The deployment of a table is itself.
	 * <li>The deployment of a view consists of:
	 * <ol>
	 * <li>Obtaining the list of all its subrelations
	 * <li>Deploy all its subrelations
	 * </ol>
	 * *
	 * </ul>
	 * 
	 * @param r
	 *            Relation to be deployed
	 * @param db
	 *            Database where r exists
	 */
	public void deploy(Relation r, Database db) {
		logger.trace("Enter deploy {}", r.fullName());

		if (r.completelyDeployed())
			logger.trace("{} already deployed", r.fullName());
		else { // if (db.getDeployingProcess(r.fullName()) == null) {
			view.status("Deploying " + r.fullName() + " this may take a few seconds...");
			DeployInBackGround deployProcess = new DeployInBackGround(db, r, view);
			// db.putDeployingProcess(r.fullName(), deployProcess);
			deployProcess.execute();

		}
		logger.trace("Exit deploy {}", r.fullName());

	}

	/**
	 * Returns the String to be displayed in the status bar when a relation is
	 * selected
	 * 
	 * @param r
	 *            The selected relation
	 * @return StatusBarMessage containing the message
	 */
	public StatusBarMessage selectedStatus(Relation r) {
		String msg1 = "";
		String msg2 = "";
		if (r instanceof Table) {
			msg1 = "Table " + r.fullName();
			if (debugging)
				msg2 = " right-click to change state";
		} else {
			msg1 = "View " + r.fullName();
			if (debugging)
				msg2 = " right-click to change state";
			/*
			 * else msg2 = " double-click to start debugging";
			 */
		}

		return new StatusBarMessage(msg1, msg2);
	}

}
