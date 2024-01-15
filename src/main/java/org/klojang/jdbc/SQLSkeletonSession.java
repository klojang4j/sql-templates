package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.jdbc.x.sql.SQLNormalizer;
import org.klojang.templates.RenderSession;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.klojang.check.CommonChecks.empty;

final class SQLSkeletonSession extends DynamicSQLSession {

  SQLSkeletonSession(Connection con, AbstractSQL sql, RenderSession session) {
    super(con, sql, session);
  }

  @Override
  public SQLQuery prepareQuery() {
    SQLInfo sqlInfo = getSQLInfo();
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(ps, this, sqlInfo);
  }

  @Override
  public SQLInsert prepareInsert(boolean retrieveKeys) {
    SQLInfo sqlInfo = getSQLInfo();
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo, retrieveKeys);
    return new SQLInsert(ps, this, sqlInfo, retrieveKeys);
  }

  @Override
  public SQLUpdate prepareUpdate() {
    SQLInfo sqlInfo = getSQLInfo();
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(ps, this, sqlInfo);
  }

  private SQLInfo getSQLInfo() {
    Check.that(session.getAllUnsetVariables()).is(empty(), sessionNotFinished(session));
    String sql = session.render();
    SQLNormalizer normalizer = new SQLNormalizer(sql);
    SQLInfo sqlInfo = new SQLInfo(sql, normalizer);
    return sqlInfo;
  }


}
