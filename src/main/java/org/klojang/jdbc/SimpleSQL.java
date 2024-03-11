package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.sql.ParamExtractor;
import org.klojang.jdbc.x.sql.ParameterInfo;

import java.sql.Connection;

import static org.klojang.jdbc.x.Strings.CONNECTION;

final class SimpleSQL extends AbstractSQL {

  private final ParameterInfo paramInfo;

  SimpleSQL(String sql, SessionConfig config, boolean isStatic) {
    super(sql, config);
    if (isStatic) {
      paramInfo = new ParameterInfo(new ParamExtractor(sql, 0));
    } else {
      paramInfo = new ParameterInfo(new ParamExtractor(sql));
    }
  }

  @Override
  public SQLSession session(Connection con) {
    Check.notNull(con, CONNECTION);
    return new SimpleSQLSession(con, this, paramInfo);
  }

}
