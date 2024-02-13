package org.klojang.jdbc.x;

public final class Err {

  public static final String NO_SUCH_SQL_TYPE
        = "no such constant in java.sql.Types: ${arg}";

  public static final String CANNOT_CONVERT_SQL_TYPE_TO_JAVA_TYPE
        = "cannot convert SQL datatype ${0} to Java type ${1}";

  public static final String NO_MORE_ROWS
        = "no more rows in result set";

  public static final String NOT_MUTABLE
        = "method not supported for immutable types ({})";
  public static final String ILLEGAL_NULL_VALUE_IN_LIST = "list must not contain null values";

  private Err() { throw new UnsupportedOperationException(); }
}
