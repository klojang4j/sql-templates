package org.klojang.jdbc;

import org.klojang.jdbc.x.JDBC;
import org.klojang.jdbc.x.sql.AbstractSQLSession;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.util.ExceptionMethods;
import org.klojang.util.ModulePrivate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Facilitates the execution of SQL UPDATE and DELETE statements.
 */
public final class SQLUpdate extends SQLStatement<SQLUpdate> {

  private final PreparedStatement ps;

  /**
   * For internal use only.
   */
  @ModulePrivate
  public SQLUpdate(Connection con, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(con, sql, sqlInfo);
    ps = JDBC.getPreparedStatement(con, sqlInfo);
  }

  /**
   * Executes the UPDATE or DELETE statement and return the number of affected rows.
   *
   * @return the number of affected rows
   */
  public int execute() {
    try {
      applyBindings(ps);
      return ps.executeUpdate();
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  @Override
  void initialize() {
    try {
      ps.clearParameters();
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sqlInfo);
    }
  }

  /**
   * Releases all resources held by this instance. You cannot reuse this instance after a
   * call to this method.
   */
  @Override
  public void close() {
    close(ps);
  }
}
