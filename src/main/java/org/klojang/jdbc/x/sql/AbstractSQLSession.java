package org.klojang.jdbc.x.sql;

import org.klojang.jdbc.*;
import org.klojang.templates.RenderSession;
import org.klojang.util.CollectionMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.function.Supplier;

import static org.klojang.check.CommonExceptions.illegalState;

public abstract sealed class AbstractSQLSession implements SQLSession
    permits BasicSQLSession, SQLTemplateSession, SQLSkeletonSession {

  @SuppressWarnings({"unused"})
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSQLSession.class);

  interface StatementFactory<T extends SQLStatement<?>> {
    T create(Connection con, AbstractSQLSession sql, SQLInfo sqlInfo);
  }

  private final AbstractSQL sql;

  AbstractSQLSession(AbstractSQL sql) {
    this.sql = sql;
  }

  public SQLQuery prepareQuery(Connection con) {
    return prepare(con, SQLQuery::new);
  }

  public SQLInsert prepareInsert(Connection con) {
    return prepare(con, SQLInsert::new);
  }

  public SQLUpdate prepareUpdate(Connection con) {
    return prepare(con, SQLUpdate::new);
  }

  public AbstractSQL getSQL() {
    return sql;
  }

  abstract <T extends SQLStatement<?>> T prepare(
      Connection con,
      StatementFactory<T> constructor);

  static Supplier<IllegalStateException> sessionNotFinished(RenderSession session) {
    String unset = CollectionMethods.implode(session.getAllUnsetVariables());
    return illegalState("one or more variables have not been set yet: " + unset);
  }

}
