package model;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.slf4j.Logger;

import logback.AreaAppender;
import model.relation.Relation;
import view.ViewInterface;
import model.relation.Database;

/**
 * The deployment of a view implies parsing its code and can freeze the gui, For
 * that reason it is done in background
 * 
 * @author rafa
 *
 */
public class DeployInBackGround extends SwingWorker<Set<Relation>, Void> {
	private Database db;
	private Relation r;
	private static final Logger logger = AreaAppender.getLogger(DeployInBackGround.class);
	private ViewInterface view;
	private boolean completelyDeployed;

	/**
	 * @param db
	 *            Reference database
	 * @param r
	 *            relation to deploy
	 * @param view
	 *            GUI interface
	 */
	public DeployInBackGround(Database db, Relation r, ViewInterface view) {
		super();
		this.db = db;
		this.r = r;
		this.view = view;
		this.completelyDeployed = false;
	}

	/**
	 * @param db
	 *            Reference database
	 * @param r
	 *            relation to deploy
	 * 
	 */
	public DeployInBackGround(Database db, Relation r) {
		super();
		this.db = db;
		this.r = r;
		this.view = null;
		this.completelyDeployed = false;
	}

	@Override
	protected Set<Relation> doInBackground() throws Exception {
		logger.trace("Starting doInBackground for relation {}", r.getName());
		view.status("Deploying view " + r.fullName() + ". This can take a few seconds...");

		final Set<Relation> subrels = r.getSubrelations();
		completelyDeploy(r, db);
		return subrels;
	}

	private void completelyDeploy(Relation r, Database db) {
		r.deploy(db);
		for (Relation subr : r.getSubrelations()) {
			if (!subr.completelyDeployed()) {
				completelyDeploy(subr, db);
			}
		}
	}

	@Override
	public void done() {
		logger.trace("Enter done for relation {}", r.getName());
		view.status("View " + r.fullName() + " deployed");

		Set<Relation> subrs;
		try {
			subrs = get();
			for (Relation subr : subrs) {
				if (!subr.completelyDeployed()) {
					logger.trace("Error: subrelation {} of {} should be completely deployed!", subr.fullName(),
							r.fullName());
					DeployInBackGround deployProcess = new DeployInBackGround(db, subr);
					deployProcess.execute();
				}
			}
			if (view != null) {
				// refresh the tree
				logger.trace("Refreshing the tree");
				view.refreshTree();
			}
			completelyDeployed = true;
		} catch (InterruptedException | ExecutionException e) {
			logger.info("Error deploying relation: {}", r, e.getStackTrace());
			e.printStackTrace();
		}

		logger.trace("Exit done for relation {}", r.getName());

	}

	public boolean isCompletelyDeployed() {
		return completelyDeployed;
	}

}
