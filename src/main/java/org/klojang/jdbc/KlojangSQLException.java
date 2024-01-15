package org.klojang.jdbc;

import org.klojang.jdbc.x.sql.SQLInfo;
import org.klojang.util.exception.UncheckedException;

import java.sql.SQLException;

/**
 * A {@link RuntimeException} that may either wrap an {@link SQLException} or indicate
 * another error condition while processing or executing SQL.
 *
 * @author Ayco Holleman
 */
public final class KlojangSQLException extends RuntimeException {

  public static KlojangSQLException wrap(Throwable exc, SQLInfo sqlInfo) {
    return wrap(exc, sqlInfo.jdbcSQL());
  }

  public static KlojangSQLException wrap(Throwable exc, SQL sql) {
    return wrap(exc, ((AbstractSQL) sql).getUnparsedSQL());
  }

  public static KlojangSQLException wrap(Throwable exc, String sql) {
    return switch (exc) {
      case KlojangSQLException e0 -> e0;
      case UncheckedException e1 -> wrap(e1.unwrap(), sql);
      default -> new KlojangSQLException(message(exc, sql), exc);
    };
  }

  public KlojangSQLException(String message) {
    super(message);
  }

  public KlojangSQLException(Throwable cause) {
    super(cause);
  }

  private KlojangSQLException(String message, Throwable cause) {
    super(message, cause);
  }

  private static String message(Throwable cause, String sql) {
    return cause.toString() + " **** while executing: " + sql;
  }

}
