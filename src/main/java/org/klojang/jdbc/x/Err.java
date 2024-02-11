package org.klojang.jdbc.x;

public final class Err {

  public static final String NO_SUCH_SQL_TYPE = "no such constant in java.sql.Types: ${arg}";
  public static final String TYPE_NOT_SUPPORTED = "type not supported: ${0}";
  public static final String NOT_CONVERTIBLE = "cannot convert ${0} to ${1}";

  private Err() { throw new UnsupportedOperationException(); }
}
