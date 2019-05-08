package model.relation;

import java.sql.Connection;
import java.util.Set;

public class Table extends Relation {

	public Table(String name, String schema, Connection conn) {
		super(name, schema, conn);
		this.deployed = true;
	}

	/**
	 * Copy constructor
	 * 
	 * @param r
	 */
	public Table(Relation r) {
		super(r);
	}

	@Override
	public boolean isView() {
		return false;
	}

	public String getIcon() {
		String result = "";
		switch (state) {
		case BUGGY:
			result = "/resources/tableBuggyIcon.png";
			break;
		case INVALID:
			result = "/resources/tableInvalidIcon.png";
			break;
		case UNKNOWN:
			result = "/resources/tableIcon.png";
			break;
		case VALID:
			result = "/resources/tableValidIcon.png";
			break;
		default:
			break;
		}

		return result;
	}

	@Override
	public Set<Relation> deploy(Database db) {
		deployed = true;
		return subrelations;
	}

}
