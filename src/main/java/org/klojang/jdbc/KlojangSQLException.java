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
public class KlojangSQLException extends RuntimeException {

  public static KlojangSQLException wrap(Throwable exc, SQLInfo sqlInfo) {
    return wrap(exc, sqlInfo.jdbcSQL());
  }

  public static KlojangSQLException wrap(Throwable exc, String sql) {
    if (exc instanceof KlojangSQLException exc0) {
      return exc0;
    } else if (exc instanceof UncheckedException exc0) {
      return wrap(exc0.unwrap(), sql);
    }
    return new KlojangSQLException(message(exc, sql), exc);
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
