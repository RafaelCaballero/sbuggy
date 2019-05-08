package control;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JOptionPane;

import org.slf4j.Logger;

import logback.AreaAppender;
import model.Model;
import model.queries.Explanation;
import model.relation.Relation;
import model.relation.RelationState;
import model.relation.View;
import view.ViewInterface;

/**
 * A trusted specification debuggging process
 * 
 * @author rafa
 *
 */
public class TrustedSpecification {
	private static final Logger logger = AreaAppender.getLogger(TrustedSpecification.class);
	private static final int WAIT = 3000;

	/**
	 * View used as trusted specification
	 */
	private View trusted;
	/**
	 * Relation to debug
	 */
	private Relation r;

	private Model mod;
	
	private ViewInterface view;

	private List<StateChange> changes;

	public TrustedSpecification(View trusted, Relation r, Model mod, ViewInterface view) {
		this.trusted = trusted;
		this.r = r;
		this.mod = mod;
		this.changes = new ArrayList<StateChange>();
		this.view = view;

	}

	private void checkTrusted() {
		if (!trusted.completelyDeployed()) {
			logger.error("Error {} not deployed!", trusted.fullName());
		}

		if (!r.completelyDeployed()) {
			try {
				view.status("Waiting for complete deployment...");
				JOptionPane.showMessageDialog(view.getFrame(), "Waiting for the deployement of relation "+r.fullName());
				Thread.sleep(WAIT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!r.completelyDeployed())
			    logger.error("Error  {} not deployed!", r.fullName());

		}

		// this can be done only if both are deployed
		if (trusted.completelyDeployed() && r.completelyDeployed()) {
			logger.trace("Starting trusted specificiation debugging...");
			Set<Relation> trustedRels = trusted.getAllSubrelations();
			String trustedSchema = trusted.getSchema();
			Set<Relation> rRels = r.getAllSubrelations();
			String rSchema = r.getSchema();
			trustedSpecification(trustedSchema, trustedRels, rSchema, rRels);
		} else
			logger.error("Error, aborting trusted specifications");
	}

	/**
	 * Recursive method that performs the debugging and writes down the state
	 * changes
	 * 
	 * @param trustedRels
	 *            all the relations in the trusted specification
	 * @param trustedSchema
	 *            schema of the trusted specification
	 * @param rSchema
	 *            of the debugged relation
	 * @param rRels
	 *            All the relations in the relation to debug
	 */
	private void trustedSpecification(String trustedSchema, Set<Relation> trustedRels, String rSchema,
			Set<Relation> rRels) {

		// first of all, all the relations in trusted are marked as valid
		for (Relation r : trustedRels)
			changes.add(new StateChange(r, RelationState.VALID, "Part of a trusted specification"));

		// now for each trusted relation, look for it with the schema of the
		// debugee relation
		for (Relation t : trustedRels) {
			String name = t.getName();
			String lookFor = rSchema + "." + name;
			for (Relation r : rRels) {
				String rName = r.fullName();
				if (rName.equals(lookFor)) {
					// now compare the two relations
					Explanation explanation = mod.compareRelations(t, r, false);
					if (explanation.getError())
						logger.error("Error comparing {} and {}. {}", t.fullName(), r.fullName(),
								explanation.getExplanation());
					else if (explanation.getAreEqual())
						changes.add(new StateChange(r, RelationState.VALID, ""));
					else
						changes.add(new StateChange(r, RelationState.INVALID, explanation.getExplanation()));
				}
			}
		}

	}

	/**
	 * Starts the process of comparing the relation with the trusted specification
	 * @return List of proposed changes.
	 */
	public List<StateChange> start() {
		checkTrusted();
		return changes;

	}

}
