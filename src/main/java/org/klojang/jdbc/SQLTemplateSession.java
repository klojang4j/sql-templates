package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.jdbc.x.sql.SQLNormalizer;
import org.klojang.templates.RenderSession;

import java.sql.Connection;

import static org.klojang.check.CommonChecks.no;

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
    close();
    var sqlInfo = getSQLInfo(session, normalizer);
    var ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(ps, this, sqlInfo);
  }

  public SQLInsert prepareInsert(boolean retrieveKeys) {
    close();
    var sqlInfo = getSQLInfo(session, normalizer);
    var ps = JDBC.getPreparedStatement(con, sqlInfo, retrieveKeys);
    return new SQLInsert(ps, this, sqlInfo, retrieveKeys);
  }

  public SQLUpdate prepareUpdate() {
    close();
    var sqlInfo = getSQLInfo(session, normalizer);
    var ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(ps, this, sqlInfo);
  }

  private static SQLInfo getSQLInfo(RenderSession session, SQLNormalizer normalizer) {
    Check.that(session.hasUnsetVariables()).is(no(), sessionNotFinished(session));
    return new SQLInfo(session.render(), normalizer);
  }


}
