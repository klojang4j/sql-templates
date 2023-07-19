package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.util.ExceptionMethods;
import org.klojang.util.ModulePrivate;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Facilitates the execution of SQL UPDATE and DELETE statements.
 */
public final class SQLUpdate extends SQLStatement<SQLUpdate> {


  /**
   * For internal use only.
   */
  @ModulePrivate
  public SQLUpdate(PreparedStatement ps, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(ps, sql, sqlInfo);
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

}
