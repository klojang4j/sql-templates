package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.jdbc.x.sql.SQLNormalizer;
import org.klojang.templates.RenderSession;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.klojang.check.CommonChecks.empty;
import static org.klojang.check.CommonExceptions.illegalState;

final class SQLTemplateSession extends DynamicSQLSession {

  private final SQLNormalizer normalizer;

  SQLTemplateSession(Connection con,
        AbstractSQL sql,
        SQLNormalizer normalizer,
        RenderSession session) {
    super(con, sql, session);
    this.normalizer = normalizer;
  }

  public SQLQuery prepareQuery() {
    Check.that(session.getAllUnsetVariables()).is(empty(), sessionNotFinished(session));
    SQLInfo sqlInfo = new SQLInfo(session.render(), normalizer);
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(ps, this, sqlInfo);
  }

  public SQLInsert prepareInsert(boolean retrieveKeys) {
    Check.that(session.getAllUnsetVariables()).is(empty(), sessionNotFinished(session));
    SQLInfo sqlInfo = new SQLInfo(session.render(), normalizer);
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo, retrieveKeys);
    return new SQLInsert(ps, this, sqlInfo, retrieveKeys);
  }

  public SQLUpdate prepareUpdate() {
    Check.that(session.getAllUnsetVariables()).is(empty(), sessionNotFinished(session));
    SQLInfo sqlInfo = new SQLInfo(session.render(), normalizer);
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(ps, this, sqlInfo);
  }

}
