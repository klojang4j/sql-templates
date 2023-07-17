package org.klojang.jdbc.x.sql;

import org.klojang.jdbc.SQLInsert;
import org.klojang.jdbc.SQLQuery;
import org.klojang.jdbc.SQLSession;
import org.klojang.jdbc.SQLUpdate;
import org.klojang.jdbc.x.JDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;

final class BasicSQLSession extends AbstractSQLSession {

  private final SQLInfo sqlInfo;

  BasicSQLSession(Connection con, AbstractSQL sql, SQLInfo sqlInfo) {
    super(con, sql);
    this.sqlInfo = sqlInfo;
  }

  @Override
  public SQLSession set(String varName, Object value) {
    throw new UnsupportedOperationException();
  }

  public SQLQuery prepareQuery() {
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(ps, this, sqlInfo);
  }

  public SQLInsert prepareInsert(boolean retrieveAutoKeys) {
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo, retrieveAutoKeys);
    return new SQLInsert(ps, this, sqlInfo, retrieveAutoKeys);
  }

  public SQLUpdate prepareUpdate() {
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(ps, this, sqlInfo);
  }


}
