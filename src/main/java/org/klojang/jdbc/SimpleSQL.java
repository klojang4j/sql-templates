package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.jdbc.x.sql.ParamExtractor;

import java.sql.Connection;

final class SimpleSQL extends AbstractSQL {

  private final SQLInfo sqlInfo;

  SimpleSQL(String sql, BindInfo bindInfo) {
    super(sql, bindInfo);
    sqlInfo = new SQLInfo(new ParamExtractor(sql));
  }

  @Override
  public SQLSession session(Connection con) {
    return new SimpleSQLSession(con, this, sqlInfo);
  }

}
