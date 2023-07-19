package org.klojang.jdbc;

import java.sql.Connection;

public abstract sealed class AbstractSQLSession implements SQLSession
      permits BasicSQLSession, SQLTemplateSession, SQLSkeletonSession {

  final Connection con;
  final AbstractSQL sql;

  AbstractSQLSession(Connection con, AbstractSQL sql) {
    this.con = con;
    this.sql = sql;
  }

  public AbstractSQL getSQL() {
    return sql;
  }

}
