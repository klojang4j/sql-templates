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

  public static final String ILLEGAL_NULL_VALUE_IN_LIST
        = "list must not contain null values";

  public static final String NO_KEYS_WERE_GENERATED
        = "cannot set ID on the provided object(s) because no keys were generated";

  public static final String TOO_MANY_KEYS
        = "actual number of database-generated keys exceeds expected number (${arg})";

  public static final String KEY_COUNT_MISMATCH
        = "actual number of database-generated keys (${arg}) does not match expected number (${obj})";

  private Err() { throw new UnsupportedOperationException(); }
}
