package org.klojang.jdbc.x;

import org.klojang.check.Check;
import org.klojang.check.ObjectCheck;
import org.klojang.jdbc.KlojangSQLException;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.util.exception.UncheckedException;

public final class Utils {

  private Utils() { throw new UnsupportedOperationException(); }

  public static <T> ObjectCheck<T, KlojangSQLException> check(T arg) {
    return Check.on(KlojangSQLException::new, arg);
  }

  public static KlojangSQLException wrap(Throwable exc, SQLInfo sqlInfo) {
    return wrap(exc, sqlInfo.jdbcSQL());
  }

  public static KlojangSQLException wrap(Throwable exc, String sql) {
    return switch (exc) {
      case KlojangSQLException e0 -> e0;
      case UncheckedException e1 -> wrap(e1.unwrap(), sql);
      default -> new KlojangSQLException(message(exc, sql), exc);
    };
  }

  public static KlojangSQLException wrap(Throwable exc) {
    return switch (exc) {
      case KlojangSQLException e0 -> e0;
      case UncheckedException e1 -> wrap(e1.unwrap());
      default -> new KlojangSQLException(exc);
    };
  }

  private static String message(Throwable cause, String sql) {
    return cause.toString() + " **** while executing: " + sql;
  }
}
