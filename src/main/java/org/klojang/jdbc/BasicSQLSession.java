package org.klojang.jdbc;

import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

final class BasicSQLSession extends AbstractSQLSession {

  private static final Logger LOG = LoggerFactory.getLogger(BasicSQLSession.class);

  private final SQLInfo sqlInfo;

  BasicSQLSession(Connection con, AbstractSQL sql, SQLInfo sqlInfo) {
    super(con, sql);
    this.sqlInfo = sqlInfo;
  }

  @Override
  public SQLQuery prepareQuery() {
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(ps, this, sqlInfo);
  }

  @Override
  public SQLInsert prepareInsert(boolean retrieveAutoKeys) {
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo, retrieveAutoKeys);
    return new SQLInsert(ps, this, sqlInfo, retrieveAutoKeys);
  }

  @Override
  public SQLUpdate prepareUpdate() {
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(ps, this, sqlInfo);
  }


  @Override
  public void execute() {
    try {
      Statement statement = con.createStatement();
      String str = sqlInfo.normalizedSQL();
      LOG.trace("Executing SQL: {}", str);
      statement.execute(str);
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sql);
    }
  }

  @Override
  public void close() { }


}
