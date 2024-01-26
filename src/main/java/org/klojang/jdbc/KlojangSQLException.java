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

  public KlojangSQLException(String message) {
    super(message);
  }

  public KlojangSQLException(Throwable cause) {
    super(cause);
  }

  public KlojangSQLException(String message, Throwable cause) {
    super(message, cause);
  }

}
