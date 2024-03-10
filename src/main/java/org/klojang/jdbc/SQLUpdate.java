package org.klojang.jdbc;

import org.klojang.jdbc.x.Msg;
import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Facilitates the execution of UPDATE, DELETE, and DDL statements.
 */
@SuppressWarnings("resource")
public final class SQLUpdate extends SQLStatement<SQLUpdate> {

  private static final Logger LOG = LoggerFactory.getLogger(SQLUpdate.class);

  SQLUpdate(PreparedStatement stmt, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(stmt, sql, sqlInfo);
  }

  /**
   * Executes the UPDATE or DELETE statement and returns the number of affected rows.
   *
   * @return the number of affected rows
   */
  public int execute() {
    LOG.trace(Msg.EXECUTING_SQL, sqlInfo.sql());
    try {
      applyBindings(stmt());
      return stmt().executeUpdate();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  @Override
  void initialize() {
    try {
      stmt().clearParameters();
    } catch (SQLException e) {
      throw Utils.wrap(e, sqlInfo);
    }
  }

}
