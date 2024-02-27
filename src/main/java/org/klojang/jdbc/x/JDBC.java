package org.klojang.jdbc.x;

import org.klojang.check.Check;
import org.klojang.invoke.Setter;
import org.klojang.invoke.SetterFactory;
import org.klojang.jdbc.SQLExpression;
import org.klojang.jdbc.x.sql.SQLInfo;

import java.sql.*;
import java.util.Map;

import static java.sql.Statement.NO_GENERATED_KEYS;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.convert.NumberMethods.convert;
import static org.klojang.invoke.NoSuchPropertyException.noSuchProperty;
import static org.klojang.jdbc.x.Err.KEY_COUNT_MISMATCH;
import static org.klojang.jdbc.x.Err.TOO_MANY_KEYS;
import static org.klojang.util.ClassMethods.box;

public final class JDBC {

  private JDBC() { throw new UnsupportedOperationException(); }

  public static String[] getColumnNames(ResultSet rs) {
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int sz = rsmd.getColumnCount();
      String[] colNames = new String[sz];
      for (int i = 0; i < rsmd.getColumnCount(); ++i) {
        colNames[i] = rsmd.getColumnLabel(i + 1);
      }
      return colNames;
    } catch (SQLException e) {
      throw Utils.wrap(e);
    }
  }

  public static PreparedStatement getPreparedStatement(Connection con, SQLInfo sqlInfo) {
    try {
      return con.prepareStatement(sqlInfo.sql());
    } catch (SQLException e) {
      throw Utils.wrap(e, sqlInfo);
    }
  }

  public static PreparedStatement getPreparedStatement(Connection con,
        SQLInfo sqlInfo,
        boolean retrieveKeys) {
    int x = retrieveKeys ? RETURN_GENERATED_KEYS : NO_GENERATED_KEYS;
    try {
      return con.prepareStatement(sqlInfo.sql(), x);
    } catch (SQLException e) {
      throw Utils.wrap(e, sqlInfo);
    }
  }

  public static long[] getGeneratedKeys(Statement stmt, int expected)
        throws SQLException {
    try (ResultSet rs = stmt.getGeneratedKeys()) {
      if (!rs.next()) {
        return new long[0];
      }
      long[] keys = new long[expected];
      int i = 0;
      do {
        Utils.check(i).is(ne(), keys.length, TOO_MANY_KEYS);
        keys[i++] = rs.getLong(1);
      } while (rs.next());
      Utils.check(i).is(eq(), expected, KEY_COUNT_MISMATCH);
      return keys;
    }
  }

  public static String quote(Statement stmt, Object value) throws SQLException {
    return switch (value) {
      case null -> "NULL";
      case Number x -> x.toString();
      case Boolean x -> x.toString();
      case SQLExpression x -> x.toString();
      default -> stmt.enquoteLiteral(value.toString());
    };
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void setID(Object bean, String idProperty, long value)
        throws Throwable {
    SetterFactory sf = SetterFactory.INSTANCE;
    Map<String, Setter> setters = sf.getSetters(bean.getClass());
    Check.that(idProperty).is(keyIn(), setters, () -> noSuchProperty(bean, idProperty));
    Setter setter = setters.get(idProperty);
    // make sure we're OK if the ID property is not a long
    Number id = convert(value, (Class) box(setter.getParamType()));
    setter.write(bean, id);
  }
}
