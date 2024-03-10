package org.klojang.jdbc;

import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.SQLInfo;

import java.sql.Connection;

final class SimpleSQLSession extends AbstractSQLSession {

  private final SQLInfo sqlInfo;

  SimpleSQLSession(Connection con, AbstractSQL sql, SQLInfo sqlInfo) {
    super(con, sql);
    this.sqlInfo = sqlInfo;
  }

  @Override
  public SQLQuery prepareQuery() {
    var stmt = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(stmt, this, sqlInfo);
  }

  @Override
  public SQLInsert prepareInsert(boolean retrieveKeys) {
    var stmt = JDBC.getPreparedStatement(con, sqlInfo, retrieveKeys);
    return new SQLInsert(stmt, this, sqlInfo, retrieveKeys);
  }

  @Override
  public SQLUpdate prepareUpdate() {
    var stmt = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(stmt, this, sqlInfo);
  }

  @Override
  public int execute() {
    return execute(sqlInfo.sql());
  }

}
