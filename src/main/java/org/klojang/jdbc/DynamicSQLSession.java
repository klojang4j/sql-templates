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

import static org.klojang.check.CommonChecks.no;
import static org.klojang.check.CommonExceptions.illegalState;
import static org.klojang.check.Tag.VARARGS;
import static org.klojang.jdbc.x.Strings.*;
import static org.klojang.util.StringMethods.append;

abstract sealed class DynamicSQLSession extends AbstractSQLSession
      permits SQLTemplateSession, SQLSkeletonSession {

  @SuppressWarnings({"unused"})
  private static final Logger LOG = LoggerFactory.getLogger(DynamicSQLSession.class);

  final RenderSession session;

  private Statement stmt;

  DynamicSQLSession(Connection con, AbstractSQL sql, RenderSession session) {
    super(con, sql);
    this.session = session;
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
        session.set(varName, ArrayMethods.implodeInts(x, ","));
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
  public final String quoteIdentifier(String identifier) {
    Check.notNull(identifier, IDENTIFIER);
    try {
      return statement().enquoteIdentifier(identifier, false);
    } catch (SQLException e) {
      close();
      throw DatabaseException.wrap(e, sql);
    }
  }

  @Override
  public final void execute() {
    try {
      Check.that(session.hasUnsetVariables()).is(no(), unfinishedSession());
      execute(session.render());
    } finally {
      close();
    }
  }


  Supplier<IllegalStateException> unfinishedSession() {
    String unset = CollectionMethods.implode(session.getAllUnsetVariables());
    return illegalState("one or more template variables have not been set yet: " + unset);
  }

  Statement statement() {
    if (stmt == null) {
      try {
        stmt = con.createStatement();
      } catch (SQLException e) {
        throw DatabaseException.wrap(e, sql);
      }
    }
    return stmt;
  }

  void close() {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
        throw DatabaseException.wrap(e, sql);
      } finally {
        stmt = null;
      }
    }
  }

}
