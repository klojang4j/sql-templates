package org.klojang.jdbc;

import org.klojang.jdbc.x.Utils;

import java.sql.SQLException;

/**
 * A {@link RuntimeException} that may either wrap a {@link SQLException} or indicate
 * another error condition while processing or executing SQL.
 *
 * @author Ayco Holleman
 */
public final class DatabaseException extends RuntimeException {

  static DatabaseException wrap(Throwable exc, SQL sql) {
    return Utils.wrap(exc, ((AbstractSQL) sql).unparsed());
  }

  /**
   * Instantiates a {@code DatabaseException} with the specified message.
   *
   * @param message the message
   */
  public DatabaseException(String message) {
    super(message);
  }

  /**
   * Instantiates a {@code DatabaseException} with the specified underlying cause.
   *
   * @param cause the cause of the exception
   */
  public DatabaseException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a {@code DatabaseException} with the specified message and underlying
   * cause.
   *
   * @param message the message
   * @param cause the cause of the exception
   */
  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }

}
