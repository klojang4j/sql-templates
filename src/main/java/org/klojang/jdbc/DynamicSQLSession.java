package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.jdbc.x.JDBC;
import org.klojang.templates.RenderSession;
import org.klojang.util.ArrayMethods;
import org.klojang.util.CollectionMethods;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.function.Supplier;

import static org.klojang.check.CommonExceptions.illegalState;

abstract sealed class DynamicSQLSession extends AbstractSQLSession
      permits SQLTemplateSession, SQLSkeletonSession {

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
  public final SQLSession setAsLiteral(String varName, Object value) {
    Check.notNull(varName, "varName");
    String val = switch (value) {
      case null -> "NULL";
      case Collection<?> x -> CollectionMethods.implode(x, this::quote, ",");
      case Number[] x -> ArrayMethods.implode(x, this::stringify, ",");
      case Boolean[] x -> ArrayMethods.implode(x, this::stringify, ",");
      case Object[] x -> ArrayMethods.implode(x, this::quote, ",");
      case char[] x -> ArrayMethods.implodeAny(x, this::quote, ",");
      default -> value.getClass().isArray()
            ? ArrayMethods.implodeAny(value, ",")
            : quote(value);
    };
    session.set(varName, val);
    return this;
  }

  static Supplier<IllegalStateException> sessionNotFinished(RenderSession session) {
    String unset = CollectionMethods.implode(session.getAllUnsetVariables());
    return illegalState("one or more variables have not been set yet: " + unset);
  }

  private String quote(Object obj) {
    try {
      return JDBC.quote(statement(), obj);
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sql);
    }
  }

  private String stringify(Object obj) {
    return obj == null ? "NULL" : obj.toString();
  }

  private Statement statement() {
    if (stmt == null) {
      try {
        stmt = con.createStatement();
      } catch (SQLException e) {
        throw KlojangSQLException.wrap(e, sql);
      }
    }
    return stmt;
  }


}
