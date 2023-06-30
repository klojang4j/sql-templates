package org.klojang.jdbc.x.sql;

import org.klojang.check.Check;
import org.klojang.jdbc.SQLSession;
import org.klojang.jdbc.SQLStatement;
import org.klojang.templates.RenderSession;
import org.klojang.util.ArrayMethods;
import org.klojang.util.CollectionMethods;

import java.sql.Connection;
import java.util.Collection;

import static org.klojang.check.CommonChecks.empty;
import static org.klojang.check.Tag.VALUE;

final class SQLTemplateSession extends AbstractSQLSession {

  private final SQLNormalizer normalizer;
  private final RenderSession session;

  SQLTemplateSession(AbstractSQL sql, SQLNormalizer normalizer, RenderSession session) {
    super(sql);
    this.normalizer = normalizer;
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

  @Override
  <T extends SQLStatement<?>> T prepare(Connection con, StatementFactory<T> constructor) {
    Check.that(session.getAllUnsetVariables()).is(empty(), sessionNotFinished(session));
    SQLInfo sqlInfo = new SQLInfo(session.render(), normalizer);
    return constructor.create(con, this, sqlInfo);
  }

}
