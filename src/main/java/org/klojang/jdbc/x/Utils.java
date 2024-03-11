package org.klojang.jdbc.x;

import org.klojang.check.Check;
import org.klojang.check.IntCheck;
import org.klojang.check.ObjectCheck;
import org.klojang.jdbc.DatabaseException;
import org.klojang.jdbc.SessionConfig;
import org.klojang.jdbc.x.sql.ParameterInfo;
import org.klojang.util.exception.UncheckedException;

import java.lang.ref.Cleaner;

public final class Utils {

  public static final SessionConfig DEFAULT_CONFIG = new SessionConfig() { };

  public static final Cleaner CENTRAL_CLEANER = Cleaner.create();

  private Utils() { throw new UnsupportedOperationException(); }

  public static <T> ObjectCheck<T, DatabaseException> check(T arg) {
    return Check.on(DatabaseException::new, arg);
  }

  public static IntCheck<DatabaseException> check(int arg) {
    return Check.on(DatabaseException::new, arg);
  }

  public static DatabaseException wrap(Throwable exc, ParameterInfo paramInfo) {
    return wrap(exc, paramInfo.normalizedSQL());
  }

  public static DatabaseException wrap(Throwable exc, String sql) {
    return switch (exc) {
      case DatabaseException e -> e;
      case UncheckedException e -> wrap(e.unwrap(), sql);
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

  public static DatabaseException exception(String message, ParameterInfo paramInfo) {
    return exception(message, paramInfo.normalizedSQL());
  }

  public static DatabaseException exception(String message, String sql) {
    return new DatabaseException(message(message, sql));
  }

  private static String message(Throwable cause, String sql) {
    return message(cause.toString(), sql);
  }

  private static String message(String message, String sql) {
    return message + " **** while executing: " + sql;
  }
}
