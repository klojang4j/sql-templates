package org.klojang.jdbc;

import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.SQLInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;

final class BasicSQLSession extends AbstractSQLSession {

  private final SQLInfo sqlInfo;

  BasicSQLSession(Connection con, AbstractSQL sql, SQLInfo sqlInfo) {
    super(con, sql);
    this.sqlInfo = sqlInfo;
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
