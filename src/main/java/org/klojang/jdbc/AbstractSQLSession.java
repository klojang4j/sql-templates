package org.klojang.jdbc;

import org.klojang.jdbc.x.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.klojang.jdbc.x.Strings.EXECUTING_SQL;

abstract sealed class AbstractSQLSession implements SQLSession
      permits SimpleSQLSession, DynamicSQLSession {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractSQLSession.class);

  final Connection con;
  final AbstractSQL sql;

  AbstractSQLSession(Connection con, AbstractSQL sql) {
    this.con = con;
    this.sql = sql;
  }

  AbstractSQL getSQL() {
    return sql;
  }

  final int execute(String sql) {
    try(Statement stmt = con.createStatement()) {
      LOG.trace(EXECUTING_SQL, sql);
      stmt.execute(sql);
      return stmt.getUpdateCount();
     } catch (SQLException e) {
      throw Utils.wrap(e, sql);
    }
  }

}
