package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.AbstractSQLSession;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.util.ExceptionMethods;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class SQLUpdate extends SQLStatement<SQLUpdate> {

  private PreparedStatement ps;

  public SQLUpdate(Connection conn, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(conn, sql, sqlInfo);
  }

  public int execute() {
    try {
      ps = con.prepareStatement(sqlInfo.jdbcSQL());
      applyBindings(ps);
      return ps.executeUpdate();
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  @Override
  public void close() {
    close(ps);
  }
}
