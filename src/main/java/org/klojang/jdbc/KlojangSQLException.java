package org.klojang.jdbc;

import org.klojang.jdbc.x.Utils;

import java.sql.SQLException;

/**
 * A {@link RuntimeException} that may either wrap an {@link SQLException} or indicate
 * another error condition while processing or executing SQL.
 *
 * @author Ayco Holleman
 */
public final class KlojangSQLException extends RuntimeException {

  static KlojangSQLException wrap(Throwable exc, SQL sql) {
    return Utils.wrap(exc, ((AbstractSQL) sql).getUnparsedSQL());
  }

  /**
   * Instantiates a {@code KlojangSQLException} with the specified message.
   *
   * @param message the message
   */
  public KlojangSQLException(String message) {
    super(message);
  }

  /**
   * Instantiates a {@code KlojangSQLException} with the specified underlying cause.
   *
   * @param cause the cause of the exception
   */
  public KlojangSQLException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a {@code KlojangSQLException} with the specified message and underlying
   * cause.
   *
   * @param message the message
   * @param cause the cause of the exception
   */
  public KlojangSQLException(String message, Throwable cause) {
    super(message, cause);
  }

}
