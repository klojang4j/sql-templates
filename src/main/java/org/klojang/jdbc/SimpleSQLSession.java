package org.klojang.jdbc;

import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.ParameterInfo;

import java.sql.Connection;

final class SimpleSQLSession extends AbstractSQLSession {

  private final ParameterInfo paramInfo;

  SimpleSQLSession(Connection con, AbstractSQL sql, ParameterInfo paramInfo) {
    super(con, sql);
    this.paramInfo = paramInfo;
  }

  @Override
  public SQLQuery prepareQuery() {
    var stmt = JDBC.getPreparedStatement(con, paramInfo);
    return new SQLQuery(stmt, this, paramInfo);
  }

  @Override
  public SQLInsert prepareInsert(boolean retrieveKeys) {
    var stmt = JDBC.getPreparedStatement(con, paramInfo, retrieveKeys);
    return new SQLInsert(stmt, this, paramInfo, retrieveKeys);
  }

  @Override
  public SQLUpdate prepareUpdate() {
    var stmt = JDBC.getPreparedStatement(con, paramInfo);
    return new SQLUpdate(stmt, this, paramInfo);
  }

  @Override
  public int execute() {
    return execute(paramInfo.normalizedSQL());
  }

}
