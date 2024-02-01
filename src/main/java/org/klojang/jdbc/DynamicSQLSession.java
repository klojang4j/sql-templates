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

import static org.klojang.check.CommonExceptions.illegalState;
import static org.klojang.check.Tag.VARARGS;
import static org.klojang.util.StringMethods.append;

abstract sealed class DynamicSQLSession extends AbstractSQLSession
      permits SQLTemplateSession, SQLSkeletonSession {

  private static final Logger LOG = LoggerFactory.getLogger(DynamicSQLSession.class);

  final RenderSession session;

  private Statement stmt;

  DynamicSQLSession(Connection con, AbstractSQL sql, RenderSession session) {
    super(con, sql);
    this.session = session;
  }

  @Override
  public final SQLSession set(String varName, Object value) {
    Check.notNull(varName, "varName");
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
    Check.notNull(varName, "varName");
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
        if (value != null && value.getClass().isArray()) {
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
    Check.notNull(varName, "varName");
    Check.notNull(identifier, "identifier");
    session.set(varName, quoteIdentifier(identifier));
    return this;
  }

  @Override
  public final String quoteValue(Object obj) {
    try {
      return JDBC.quote(statement(), obj);
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sql);
    }
  }

  @Override
  public final SQLExpression sqlFunction(String name, Object... args) {
    Check.notNull(name, "SQL function name");
    Check.notNull(args, VARARGS);
    String str = ArrayMethods.implode(args, this::quoteValue, ",");
    String expr = append(new StringBuilder(), name, '(', str, ')').toString();
    return new SQLExpression(expr);
  }

  @Override
  public final String quoteIdentifier(String obj) {
    try {
      return statement().enquoteIdentifier(obj, false);
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sql);
    }
  }

  @Override
  public final void execute() {
    try {
      Statement statement = con.createStatement();
      String str = session.render();
      LOG.trace("Executing SQL: {}", str);
      statement.execute(str);
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sql);
    }
  }


  static Supplier<IllegalStateException> sessionNotFinished(RenderSession session) {
    String unset = CollectionMethods.implode(session.getAllUnsetVariables());
    return illegalState("one or more variables have not been set yet: " + unset);
  }

  Statement statement() {
    if (stmt == null) {
      try {
        stmt = con.createStatement();
      } catch (SQLException e) {
        throw KlojangSQLException.wrap(e, sql);
      }
    }
    return stmt;
  }

  public void close() {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
        throw KlojangSQLException.wrap(e, sql);
      } finally {
        stmt = null;
      }
    }
  }

}
