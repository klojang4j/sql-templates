package org.klojang.jdbc.x.sql;

import org.klojang.jdbc.SQLSession;
import org.klojang.jdbc.SQLStatement;

import java.sql.Connection;

final class BasicSQLSession extends AbstractSQLSession {

  private final SQLInfo sqlInfo;

  BasicSQLSession(AbstractSQL sql, SQLInfo sqlInfo) {
    super(sql);
    this.sqlInfo = sqlInfo;
  }

  @Override
  public SQLSession set(String varName, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  <T extends SQLStatement<?>> T prepare(Connection con, StatementFactory<T> constructor) {
    return constructor.create(con, this, sqlInfo);
  }

}
