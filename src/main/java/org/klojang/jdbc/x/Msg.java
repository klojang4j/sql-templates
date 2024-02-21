package org.klojang.jdbc.x;

public final class Msg {

  public static final String NO_PREDEFINED_BINDER
        = "No predefined binder for values of type {}. Will call {}.toString()";

  public static final String NO_PREDEFINED_TYPE_MAPPING
        = "No predefined mapping exists between {} and {}. Will call PreparedStatement.setObject()";

  public static final String NO_PREDEFINED_COLUMN_READER
        = "No predefined column reader exists for {}. Will attempt to create a custom reader for column type {}";

  public static final String EXECUTING_SQL
        = "Executing SQL: {}";

  private Msg() { throw new UnsupportedOperationException(); }
}
