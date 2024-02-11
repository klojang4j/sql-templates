package org.klojang.jdbc.x;

public final class Err {

  public static final String NO_SUCH_SQL_TYPE
        = "no such constant in java.sql.Types: ${arg}";

  public static final String CANNOT_CONVERT_SQL_TYPE_TO_JAVA_TYPE
        = "Cannot convert SQL datatype ${0} to Java type ${1}";

  private Err() { throw new UnsupportedOperationException(); }
}
