package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.AbstractSQLSession;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.util.ExceptionMethods;
import org.klojang.util.ModulePrivate;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Facilitates the execution of SQL UPDATE and DELETE statements.
 */
public final class SQLUpdate extends SQLStatement<SQLUpdate> {

  private PreparedStatement ps;

  /**
   * For internal use only.
   */
  @ModulePrivate
  public SQLUpdate(Connection conn, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(conn, sql, sqlInfo);
  }

  /**
   * Executes the UPDATE or DELETE statement and return the number of affected rows.
   * @return the number of affected rows
   */
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
