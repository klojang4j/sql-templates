package org.klojang.jdbc.x;

public final class Err {

  public static final String NO_PREDEFINED_BINDER
        = "No predefined binder for values of type {}. Will call PreparedStatement.setObject()";

  public static final String NO_PREDEFINED_TYPE_MAPPING
        = "No predefined mapping exists between {} and {}. Will call PreparedStatement.setObject()";

  private Err() { throw new UnsupportedOperationException(); }
}
