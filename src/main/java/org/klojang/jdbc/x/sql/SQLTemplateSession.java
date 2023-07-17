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
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.empty;
import static org.klojang.check.CommonExceptions.illegalState;
import static org.klojang.check.Tag.VALUE;

final class SQLTemplateSession extends AbstractSQLSession {

  private final SQLNormalizer normalizer;
  private final RenderSession session;

  SQLTemplateSession(Connection con,
        AbstractSQL sql,
        SQLNormalizer normalizer,
        RenderSession session) {
    super(con, sql);
    this.normalizer = normalizer;
    this.session = session;
  }

  static Supplier<IllegalStateException> sessionNotFinished(RenderSession session) {
    String unset = CollectionMethods.implode(session.getAllUnsetVariables());
    return illegalState("one or more variables have not been set yet: " + unset);
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
    Check.that(session.getAllUnsetVariables()).is(empty(), sessionNotFinished(session));
    SQLInfo sqlInfo = new SQLInfo(session.render(), normalizer);
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLQuery(ps, this, sqlInfo);
  }

  public SQLInsert prepareInsert(boolean retrieveAutoKeys) {
    Check.that(session.getAllUnsetVariables()).is(empty(), sessionNotFinished(session));
    SQLInfo sqlInfo = new SQLInfo(session.render(), normalizer);
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo, retrieveAutoKeys);
    return new SQLInsert(ps, this, sqlInfo, retrieveAutoKeys);
  }

  public SQLUpdate prepareUpdate() {
    Check.that(session.getAllUnsetVariables()).is(empty(), sessionNotFinished(session));
    SQLInfo sqlInfo = new SQLInfo(session.render(), normalizer);
    PreparedStatement ps = JDBC.getPreparedStatement(con, sqlInfo);
    return new SQLUpdate(ps, this, sqlInfo);
  }


}
