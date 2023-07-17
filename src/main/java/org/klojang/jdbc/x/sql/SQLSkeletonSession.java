package org.klojang.jdbc.x.sql;

import org.klojang.check.Check;
import org.klojang.jdbc.SQLInsert;
import org.klojang.jdbc.SQLQuery;
import org.klojang.jdbc.SQLSession;
import org.klojang.jdbc.SQLUpdate;
import org.klojang.jdbc.x.JDBC;
import org.klojang.templates.RenderSession;
import org.klojang.util.ArrayMethods;
import org.klojang.util.CollectionMethods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;

import static org.klojang.check.Tag.VALUE;

final class SQLSkeletonSession extends AbstractSQLSession {

  private final RenderSession session;

  SQLSkeletonSession(Connection con, AbstractSQL sql, RenderSession session) {
    super(con, sql);
    this.session = session;
  }

  @Override
  public SQLSession set(String varName, Object value) {
    Check.notNull(varName, "varName");
    Check.notNull(value, VALUE);
    if (value instanceof Collection<?> c) {
      session.set(varName, CollectionMethods.implode(c));
    } else if (value.getClass().isArray()) {
      session.set(varName, ArrayMethods.implodeAny(value));
    } else {
      session.set(varName, value);
    }
    return this;
  }

  public SQLQuery prepareQuery() {
    SQLNormalizer normalizer = new SQLNormalizer(session.render());
    SQLInfo sqlInfo = new SQLInfo(session.render(), normalizer);
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(ps, this, sqlInfo);
  }

  public SQLInsert prepareInsert(boolean retrieveAutoKeys) {
    SQLNormalizer normalizer = new SQLNormalizer(session.render());
    SQLInfo sqlInfo = new SQLInfo(session.render(), normalizer);
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo, retrieveAutoKeys);
    return new SQLInsert(ps, this, sqlInfo, retrieveAutoKeys);
  }

  public SQLUpdate prepareUpdate() {
    SQLNormalizer normalizer = new SQLNormalizer(session.render());
    SQLInfo sqlInfo = new SQLInfo(session.render(), normalizer);
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(ps, this, sqlInfo);
  }


}
