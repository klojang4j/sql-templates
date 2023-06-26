package org.klojang.jdbc.x.sql;

import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.SQL;
import org.klojang.jdbc.SQLStatement;

import java.sql.Connection;

public final class ParametrizedSQL extends AbstractSQL {

  private final SQLNormalizer normalizer;

  public ParametrizedSQL(String sql, BindInfo bindInfo) {
    super(sql, bindInfo);
    this.normalizer = new SQLNormalizer(sql);
  }

  @Override
  public SQL set(String varName, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unlock() {}

  @Override
  <T extends SQLStatement<?>> T prepare(Connection con, StatementFactory<T> constructor) {
    return constructor.create(con, this, new SQLInfo(sql, normalizer));
  }

}
