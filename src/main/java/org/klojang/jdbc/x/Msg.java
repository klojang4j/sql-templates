package org.klojang.jdbc.x;

public final class Msg {

  public static final String NO_PREDEFINED_BINDER
        = "No predefined binder for values of type {}. Will call PreparedStatement.setObject()";

  public static final String NO_PREDEFINED_TYPE_MAPPING
        = "No predefined mapping exists between {} and {}. Will call PreparedStatement.setObject()";
  public static final String NO_PREDEFINED_COLUMN_READER
              = "No predefined ColumnReader exists for {}. Searching for factory method on {}";

  private Msg() { throw new UnsupportedOperationException(); }
}
