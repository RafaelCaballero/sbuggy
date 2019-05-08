package control;

import model.relation.Relation;
import model.relation.RelationState;

/**
 * A propose of state change still not applied
 * 
 * @author rafa
 *
 */
public class StateChange {
	private Relation r;
	private RelationState state;
	private String explanation;

	public StateChange(Relation r, RelationState state, String explanation) {
		this.r = r;
		this.state = state;
		this.explanation = explanation;
	}

	/**
	 * @return the r
	 */
	public Relation getR() {
		return r;
	}

	/**
	 * @param r
	 *            the r to set
	 */
	public void setR(Relation r) {
		this.r = r;
	}

	/**
	 * @return the state
	 */
	public RelationState getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(RelationState state) {
		this.state = state;
	}

	/**
	 * @return the explanation
	 */
	public String getExplanation() {
		return explanation;
	}

	/**
	 * @param explanation
	 *            the explanation to set
	 */
	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	@Override
	public String toString() {
		String result = "Relation: " + r.fullName() + " state: " + this.state + " explanation: " + explanation;
		return result;
	}

}
