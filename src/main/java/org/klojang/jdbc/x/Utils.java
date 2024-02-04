package org.klojang.jdbc.x;

import org.klojang.check.Check;
import org.klojang.check.ObjectCheck;
import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.util.exception.UncheckedException;

public final class Utils {

  private Utils() { throw new UnsupportedOperationException(); }

  public static <T> ObjectCheck<T, DatabaseException> check(T arg) {
    return Check.on(DatabaseException::new, arg);
  }

  public static DatabaseException wrap(Throwable exc, SQLInfo sqlInfo) {
    return wrap(exc, sqlInfo.sql());
  }

  public static DatabaseException wrap(Throwable exc, String sql) {
    return switch (exc) {
      case DatabaseException e0 -> e0;
      case UncheckedException e1 -> wrap(e1.unwrap(), sql);
      default -> new DatabaseException(message(exc, sql), exc);
    };
  }

  public static DatabaseException wrap(Throwable exc) {
    return switch (exc) {
      case DatabaseException e0 -> e0;
      case UncheckedException e1 -> wrap(e1.unwrap());
      default -> new DatabaseException(exc);
    };
  }

  private static String message(Throwable cause, String sql) {
    return cause.toString() + " **** while executing: " + sql;
  }
}
