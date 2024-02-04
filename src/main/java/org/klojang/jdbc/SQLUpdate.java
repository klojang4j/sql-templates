package org.klojang.jdbc;

import org.klojang.jdbc.x.Utils;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.klojang.jdbc.x.Strings.EXECUTING_SQL;

/**
 * Facilitates the execution of UPDATE, DELETE and DDL statements.
 */
public final class SQLUpdate extends SQLStatement<SQLUpdate> {

  private static final Logger LOG = LoggerFactory.getLogger(SQLUpdate.class);

  SQLUpdate(PreparedStatement ps, AbstractSQLSession sql, SQLInfo sqlInfo) {
    super(ps, sql, sqlInfo);
  }

  /**
   * Executes the UPDATE or DELETE statement and returns the number of affected rows.
   *
   * @return the number of affected rows
   */
  public int execute() {
    LOG.trace(EXECUTING_SQL, sqlInfo.jdbcSQL());
    try {
      applyBindings(ps);
      return ps.executeUpdate();
    } catch (Throwable t) {
      throw Utils.wrap(t);
    }
  }

  @Override
  void initialize() {
    try {
      ps.clearParameters();
    } catch (SQLException e) {
      throw Utils.wrap(e, sqlInfo);
    }
  }

}
