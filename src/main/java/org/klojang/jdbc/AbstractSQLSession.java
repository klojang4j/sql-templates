package org.klojang.jdbc;

import java.sql.Connection;

abstract sealed class AbstractSQLSession implements SQLSession
      permits BasicSQLSession, DynamicSQLSession {

  final Connection con;
  final AbstractSQL sql;

  AbstractSQLSession(Connection con, AbstractSQL sql) {
    this.con = con;
    this.sql = sql;
  }

  AbstractSQL getSQL() {
    return sql;
  }

}
