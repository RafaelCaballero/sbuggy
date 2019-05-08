package model.relation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;

import logback.AreaAppender;
import model.antlr4.SqlLexer;
import model.antlr4.SqlParser;
import model.antlr4.SqlRelationsListener;

public class View extends Relation {

	private static final Logger logger = AreaAppender.getLogger(View.class);

	private String def;

	/**
	 * Constructs a view given its name. The definition is empty.
	 * 
	 * @param name
	 */
	private View(String name, String schema, Connection conn) {
		super(name, schema, conn);
		this.def = "";

	}

	/**
	 * Constructs a view given its name and definition
	 * 
	 * @param name
	 *            View name
	 * @param def
	 *            Code defining the view
	 */
	public View(String name, String schema, String def, Connection conn) {
		super(name, schema, conn);
		this.def = def;
	}

	/**
	 * Copy constructor
	 * 
	 * @param rprime
	 */
	public View(Relation r) {
		super(r);
	}

	@Override
	public Set<Relation> deploy(Database db) {
		Set<String> srls = null;
		// create substree
		try {
			srls = subtree();
			deployed = true;

			// convert the strings into relations
			for (String s : srls) {
				Relation sr = db.getRelation(s);
				if (sr != null)
					subrelations.add(sr);
			}

		} catch (IOException e) {
			logger.error("Error analyzing definition of view: {}. Message {}", this.getName(), e.getStackTrace());
		}
		return subrelations;
	}

	@Override
	public boolean isView() {
		return true;
	}

	public String getIcon() {
		String result = "";
		switch (state) {
		case BUGGY:
			result = "/resources/viewBuggyIcon.png";
			;
			break;
		case INVALID:
			result = "/resources/viewInvalidIcon.png";
			break;
		case UNKNOWN:
			result = "/resources/viewIcon.png";
			;
			break;
		case VALID:
			result = "/resources/viewValidIcon.png";
			break;
		default:
			break;
		}

		return result;
	}

	public String getDef() {
		return def;
	}

	private Set<String> subtree() throws IOException {
		if (def.endsWith(";"))
			def = def.substring(0, def.length() - 1);
		InputStream is = new ByteArrayInputStream(def.getBytes(StandardCharsets.UTF_8));

		ANTLRInputStream input = new ANTLRInputStream(is);

		// prepare the parser
		SqlLexer lexer = new SqlLexer(input);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		SqlParser parser = new SqlParser(tokens);

		ParseTree tree = parser.selectStmt(); // parse; start at model

		// prepare the listener
		ParseTreeWalker walker = new ParseTreeWalker();

		SqlRelationsListener extractor = new SqlRelationsListener(); // (parser,pu);
		walker.walk(extractor, tree);
		// System.out.println(tree.toStringTree(parser)); // print tree as text
		// <label id="code.tour.main.7"/>
		return extractor.getSubrelations();

	}

}
