package org.klojang.jdbc;

import org.klojang.check.Check;
import org.klojang.invoke.BeanReader;
import org.klojang.jdbc.x.JDBC;
import org.klojang.templates.RenderSession;
import org.klojang.util.ArrayMethods;
import org.klojang.util.CollectionMethods;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.empty;
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
  public final SQLSession quote(String varName, Object value) {
    Check.notNull(varName, "varName");
    session.set(varName, quoteValue(value));
    return this;
  }

//  @SuppressWarnings("unchecked")
//  public final <T> SQLSession setValues(T[] beans) {
//    Class<T> clazz = (Class<T>) beans.getClass().getComponentType();
//    BeanReader<T> reader = new BeanReader<>(clazz);
//    return setValues(reader, Arrays.asList(beans));
//  }

  public final <T> SQLSession setValues(Class<T> beanClass, List<T> beans) {
    Check.notNull(beanClass, "beanClass");
    Check.that(beans, "beans").isNot(empty());
    String[] vars = session.getTemplate().getVariables().toArray(String[]::new);
    BeanReader<T> reader = new BeanReader<>(beanClass);
    List<Map<String, String>> quoted = new ArrayList<>(beans.size());
    for (T bean : beans) {
      Map<String, String> m = HashMap.newHashMap(vars.length);
      for (String var : vars) {
        m.put(var, quoteValue(reader.read(bean, var)));
      }
      quoted.add(m);
    }
    session.populate("values", quoted, ",");
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
      statement.execute(session.render());
    } catch (SQLException e) {
      throw KlojangSQLException.wrap(e, sql);
    }
  }


  static Supplier<IllegalStateException> sessionNotFinished(RenderSession session) {
    String unset = CollectionMethods.implode(session.getAllUnsetVariables());
    return illegalState("one or more variables have not been set yet: " + unset);
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

  public void close() {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
        throw KlojangSQLException.wrap(e, sql);
      }
    }
  }

}
