package model.relation;

import view.ViewInterface;

public enum RelationState {
	VALID(ViewInterface.STATEVALID), INVALID(ViewInterface.STATEINVALID), UNKNOWN(ViewInterface.STATEUNKNOWN), BUGGY(
			"Buggy");

	// Member to hold the name
	private String string;

	/**
	 * constructor to set the string
	 * 
	 * @param name
	 */
	RelationState(String name) {
		string = name;
	}

	@Override
	public String toString() {
		return string;
	}

}
