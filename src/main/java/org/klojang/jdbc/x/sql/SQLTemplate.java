package org.klojang.jdbc.x.sql;

import org.klojang.check.Check;
import org.klojang.jdbc.BindInfo;
import org.klojang.jdbc.KlojangSQLException;
import org.klojang.jdbc.SQL;
import org.klojang.jdbc.SQLStatement;
import org.klojang.templates.ParseException;
import org.klojang.templates.RenderSession;
import org.klojang.templates.Template;
import org.klojang.util.ArrayMethods;
import org.klojang.util.CollectionMethods;

import java.sql.Connection;
import java.util.Collection;

import static org.klojang.check.CommonChecks.empty;
import static org.klojang.check.Tag.VALUE;

public final class SQLTemplate extends AbstractSQL {

  private final SQLNormalizer normalizer;
  private final Template template;
  private RenderSession session;

  public SQLTemplate(String sql, BindInfo bindInfo) {
    super(sql, bindInfo);
    normalizer = new SQLNormalizer(sql);
    try {
      template = Template.fromString(normalizer.getNormalizedSQL());
    } catch (ParseException e) {
      throw new KlojangSQLException(e);
    }
  }

  @Override
  public SQL set(String varName, Object value) {
    Check.notNull(varName, "varName");
    Check.notNull(value, VALUE);
    if (session == null) {
      session = template.newRenderSession();
    }
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
  public void unlock() {
    session = null;
  }

  @Override
  <T extends SQLStatement<?>> T prepare(Connection con, StatementFactory<T> constructor) {
    if (session == null) {
      session = template.newRenderSession();
    }
    Check.that(session.getAllUnsetVariables()).is(empty(), sessionNotFinished(session));
    return constructor.create(con, this, new SQLInfo(sql, session.render(), normalizer));
  }

}
