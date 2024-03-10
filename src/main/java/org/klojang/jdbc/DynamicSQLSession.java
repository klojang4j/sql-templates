package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.JDBC;
import org.klojang.templates.RenderSession;
import org.klojang.util.ArrayMethods;
import org.klojang.util.CollectionMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.function.Supplier;

import static java.lang.ref.Cleaner.Cleanable;
import static org.klojang.check.CommonExceptions.illegalState;
import static org.klojang.check.Tag.VARARGS;
import static org.klojang.jdbc.x.Strings.*;
import static org.klojang.jdbc.x.Utils.CENTRAL_CLEANER;
import static org.klojang.util.StringMethods.append;

abstract sealed class DynamicSQLSession extends AbstractSQLSession
      permits SQLTemplateSession, SQLSkeletonSession {

  private static final Logger LOG = LoggerFactory.getLogger(DynamicSQLSession.class);

  private final StatementContainer stmt;
  private final Cleanable cleanable;

  final RenderSession session;

  DynamicSQLSession(Connection con, AbstractSQL sql, RenderSession session) {
    super(con, sql);
    this.session = session;
    this.stmt = new StatementContainer();
    this.cleanable = CENTRAL_CLEANER.register(this, stmt);
  }

  @Override
  public final SQLSession set(String varName, Object value) {
    Check.notNull(varName, VAR_NAME);
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
  public final SQLSession setValue(String varName, Object value) {
    Check.notNull(varName, VAR_NAME);
    switch (value) {
      case Collection<?> x -> {
        String val = CollectionMethods.implode(x, this::quoteValue, ",");
        session.set(varName, val);
      }
      case Object[] x -> {
        String val = ArrayMethods.implode(x, this::quoteValue, ",");
        session.set(varName, val);
      }
      case int[] x -> {
        String val = ArrayMethods.implodeInts(x, ",");
        session.set(varName, val);
      }
      default -> {
        if (value.getClass().isArray()) {
          session.set(varName, ArrayMethods.implodeAny(value, this::quoteValue, ","));
        } else {
          session.set(varName, quoteValue(value));
        }
      }
    }
    return this;
  }

  @Override
  public final SQLSession setArray(String varName, Object... values) {
    Check.notNull(varName, VAR_NAME);
    Check.notNull(values, VARARGS);
    String val = ArrayMethods.implode(values, this::quoteValue, ",");
    session.set(varName, val);
    return this;
  }

  @Override
  public final SQLSession setArray(String varName, int... values) {
    Check.notNull(varName, VAR_NAME);
    Check.notNull(values, VARARGS);
    String val = ArrayMethods.implodeInts(values, ",");
    session.set(varName, val);
    return this;
  }

  @Override
  public final SQLSession setIdentifier(String varName, String identifier) {
    Check.notNull(varName, VAR_NAME);
    Check.notNull(identifier, IDENTIFIER);
    session.set(varName, quoteIdentifier(identifier));
    return this;
  }

  @Override
  public final SQLSession setOrderBy(String sortColumn) {
    Check.notNull(sortColumn, SORT_COLUMN);
    return setIdentifier(ORDER_BY, sortColumn);
  }

  @Override
  public final SQLSession setOrderBy(String sortColumn, boolean isDescending) {
    String orderBy = quoteIdentifier(sortColumn) + (isDescending ? " DESC" : " ASC");
    return set(ORDER_BY, orderBy);
  }


  @Override
  public final String quoteValue(Object obj) {
    try {
      return JDBC.quote(statement(), obj);
    } catch (SQLException e) {
      close();
      throw DatabaseException.wrap(e, sql);
    }
  }

  @Override
  public final SQLExpression sqlFunction(String name, Object... args) {
    Check.notNull(name, FUNCTION_NAME);
    Check.notNull(args, VARARGS);
    String str = ArrayMethods.implode(args, this::quoteValue, ",");
    String expr = append(new StringBuilder(), name, '(', str, ')').toString();
    return new SQLExpression(expr);
  }

  @Override
  public final int execute() {
    try {
      Check.that(session).isNot(RenderSession::hasUnsetVariables, rogueVariables());
      return execute(session.render());
    } finally {
      close();
    }
  }

  @Override
  @SuppressWarnings("resource")
  public final String quoteIdentifier(String identifier) {
    Check.notNull(identifier, IDENTIFIER);
    try {
      return statement().enquoteIdentifier(identifier, false);
    } catch (SQLException e) {
      close();
      throw DatabaseException.wrap(e, sql);
    }
  }


  Statement statement() {
    try {
      return stmt.get(con);
    } catch (SQLException e) {
      throw DatabaseException.wrap(e, sql);
    }
  }

  void close() {
    cleanable.clean();
  }

  Supplier<IllegalStateException> rogueVariables() {
    String unset = CollectionMethods.implode(session.getAllUnsetVariables());
    return illegalState("unset variables in SQL template: " + unset);
  }

  private static class StatementContainer implements Runnable {

    private Statement stmt;

    Statement get(Connection con) throws SQLException {
      if (stmt == null) {
        stmt = con.createStatement();
      }
      return stmt;
    }

    @Override
    public void run() {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (Throwable t) {
          LOG.error(t.toString());
        } finally {
          stmt = null;
        }
      }
    }
  }


}
