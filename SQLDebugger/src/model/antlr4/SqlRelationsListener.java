package model.antlr4;

import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.misc.NotNull;
import org.slf4j.Logger;

import logback.AreaAppender;

@SuppressWarnings("deprecation")
public class SqlRelationsListener extends SqlBaseListener {
	private Set<String> relations;
	private static final Logger logger = AreaAppender.getLogger(SqlRelationsListener.class);

	public SqlRelationsListener() {
		relations = new HashSet<String>();
	}

	@Override
	public void exitSelectStmt(@NotNull SqlParser.SelectStmtContext ctx) {
		logger.trace("Parsed: " + ctx.getText());
	}

	@Override
	public void exitQualified_name(@NotNull SqlParser.Qualified_nameContext ctx) {
		relations.add(ctx.getText());
	}

	public Set<String> getSubrelations() {
		return relations;
	}

}
