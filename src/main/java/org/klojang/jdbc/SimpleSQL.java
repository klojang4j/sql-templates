package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.ParamExtractor;
import org.klojang.jdbc.x.sql.SQLInfo;

import java.sql.Connection;

final class SimpleSQL extends AbstractSQL {

  private final SQLInfo sqlInfo;

  SimpleSQL(String sql, boolean isStatic, BindInfo bindInfo) {
    super(sql, bindInfo);
    if (isStatic) {
      sqlInfo = new SQLInfo(new ParamExtractor(sql, 0));
    } else {
      sqlInfo = new SQLInfo(new ParamExtractor(sql));
    }
  }

  @Override
  public SQLSession session(Connection con) {
    return new SimpleSQLSession(con, this, sqlInfo);
  }

}
