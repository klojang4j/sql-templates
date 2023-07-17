package org.klojang.jdbc.x.sql;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.SQLSession;

import java.sql.Connection;

public final class BasicSQL extends AbstractSQL {

  private final SQLInfo sqlInfo;

  public BasicSQL(String sql, BindInfo bindInfo) {
    super(sql, bindInfo);
    sqlInfo = new SQLInfo(new SQLNormalizer(sql));
  }

  public SQLSession session(Connection con) {
    return new BasicSQLSession(con, this, sqlInfo);
  }

}
