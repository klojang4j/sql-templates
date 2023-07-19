package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.jdbc.x.sql.SQLNormalizer;

import java.sql.Connection;

final class BasicSQL extends AbstractSQL {

  private final SQLInfo sqlInfo;

  BasicSQL(String sql, BindInfo bindInfo) {
    super(sql, bindInfo);
    sqlInfo = new SQLInfo(new SQLNormalizer(sql));
  }

  @Override
  public SQLSession session(Connection con) {
    return new BasicSQLSession(con, this, sqlInfo);
  }

}
