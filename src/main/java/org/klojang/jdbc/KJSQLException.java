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
public class KJSQLException extends RuntimeException{

  static KJSQLException wrap(Throwable exc, SQLInfo sqlInfo) {
    if (exc instanceof KJSQLException exc0) {
      return exc0;
    } else if (exc instanceof UncheckedException exc0) {
      return wrap(exc0.unwrap(), sqlInfo);
    }
    return new KJSQLException(message(sqlInfo, exc), exc);
  }

  public KJSQLException(String message) {
    super(message);
  }

  public KJSQLException(Throwable cause) {
    super(cause);
  }

  private KJSQLException(String message, Throwable cause) {
    super(message, cause);
  }


  private static String message(SQLInfo sqlInfo, Throwable cause) {
    return cause.getMessage() + " >>>> while executing: " + sqlInfo;
  }

}
